package com.clinic.dentist.services;

import com.clinic.dentist.models.Patient;
import com.clinic.dentist.models.Role;
import com.clinic.dentist.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class PatientService {
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private PatientRepository patientRepository;

    public void addPatient(Patient patient) {
        patient.setActive(true);
        patient.setRoles(Collections.singleton(Role.ADMIN));
        patient.setPassword(bCryptPasswordEncoder.encode(patient.getPassword()));
        patientRepository.save(patient);
    }

    public boolean checkPatient(Patient patient) {

        if (patientRepository.findByUsername(patient.getUsername()).isPresent()) {
            return true;
        }
        return false;

    }

    public Patient correctData(Patient patient) {
        patient.setFirstName(patient.getFirstName().trim());
        patient.setLastName(patient.getLastName().trim());
        patient.setUsername(patient.getUsername().trim());
        return patient;
    }

    public Patient findUser(String login, String password) {
        password = bCryptPasswordEncoder.encode(password);
        Patient patient = patientRepository.findByUsername(login).orElseThrow(RuntimeException::new);
       patient.getPassword();
         if (patient.getPassword().equals(password)) {
            return patient;
        } else {
            throw new RuntimeException("Invalid password");
        }
    }
}
