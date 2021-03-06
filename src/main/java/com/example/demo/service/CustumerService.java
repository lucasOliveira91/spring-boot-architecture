package com.example.demo.service;

import com.example.demo.domain.Address;
import com.example.demo.domain.City;
import com.example.demo.domain.Custumer;
import com.example.demo.domain.enums.CustumerType;
import com.example.demo.domain.enums.Role;
import com.example.demo.dto.CustumerDTO;
import com.example.demo.dto.CustumerNewDTO;
import com.example.demo.exception.AuthorizationException;
import com.example.demo.exception.DataIntegrityException;
import com.example.demo.exception.ObjectNotFoundException;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.CustumerRepository;
import com.example.demo.security.UserSS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;

/**
 * Created by loliveira on 16/11/18.
 */
@Service
public class CustumerService {

    @Autowired
    private CustumerRepository custumerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ImageService imageService;

    @Value("${img.prefix.client.profile}")
    private String prefix;

    public Custumer find(Integer id) {
        UserSS user = UserService.Authenticated();

        if(user == null || !user.hasRole(Role.ADMIN) && !user.getId().equals(id)) {
            throw new AuthorizationException("Access Denied");
        }

        return custumerRepository.findById(id).orElseThrow(() ->
            new ObjectNotFoundException("Object not found! Id:" + id + " " + Custumer.class.getName()));
    }

    @Transactional
    public Custumer insert(Custumer custumer) {
        Custumer save = custumerRepository.save(custumer);
        addressRepository.saveAll(save.getAddresses());
        return save;
    }

    public Custumer update(CustumerDTO obj) {
        Custumer custumer = find(obj.getId());
        return custumerRepository.save(updateData(obj, custumer));
    }

    private Custumer updateData(CustumerDTO obj, Custumer custumer) {
        custumer.setName(obj.getName());
        custumer.setEmail(obj.getEmail());

        return custumer;
    }

    public void delete(Integer id) {
        find(id);

        try{
            custumerRepository.deleteById(id);
        }catch (DataIntegrityViolationException ex){
            throw new DataIntegrityException("we cannot delete, there're relations.");
        }
    }

    public List<Custumer> findAll() {
        return custumerRepository.findAll();
    }

    public Custumer findByEmail(String email) {
        UserSS user = UserService.Authenticated();

        if(user == null || !user.hasRole(Role.ADMIN) && !user.getUsername().equals(email)) {
            throw new AuthorizationException("Access Denied");
        }

        Custumer custumer = custumerRepository.findByEmail(email);
        if(custumer == null) {
            throw new ObjectNotFoundException("User not found: " + email + " id: " + user.getId());
        }

        return custumer;
    }

    public Page<Custumer> findAllPageable(Integer page, Integer linesPerPage, String orderBy, String direction  ) {
        PageRequest pageRequest = PageRequest.of(page, linesPerPage, Sort.Direction.valueOf(direction), orderBy);
        return custumerRepository.findAll(pageRequest);
    }


    public Custumer fromDTO(CustumerNewDTO obj) {
        Custumer custumer = new Custumer(null, obj.getName(), obj.getEmail(), obj.getCpfCnpj(), CustumerType.toEnum(obj.getCustumerType()), passwordEncoder.encode(obj.getPassword()));
        Address adress = new Address(null, obj.getPublicPlace(),
                obj.getNeighborhood(), obj.getComplement(),obj.getNumber(),obj.getZipCOde(),
                new City(obj.getCityId(), null, null), custumer);
        custumer.getAddresses().add(adress);
        custumer.getCelPhones().add(obj.getCelPhone1());

        if(obj.getCelPhone2() != null) {
            custumer.getCelPhones().add(obj.getCelPhone2());
        }

        if(obj.getCelPhone3() != null) {
            custumer.getCelPhones().add(obj.getCelPhone2());
        }

        return custumer;
    }

    public URI uploadProfilePicture(MultipartFile multipartFile) {
        UserSS user = UserService.Authenticated();
        if(user == null) {
            throw new AuthorizationException("Access Denied.");
        }

        BufferedImage jpgImage = imageService.cropAndResize(imageService.getJpgImageFromFile(multipartFile));
        String fileName = prefix + user.getId() + ".jpg";

        return s3Service.uploadFile(fileName, imageService.getInputStream(jpgImage, "jpg"), "image");
    }
}
