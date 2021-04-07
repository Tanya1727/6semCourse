package com.clinic.dentist.services;

import com.clinic.dentist.models.Clinic;
import com.clinic.dentist.models.Maintenance;
import com.clinic.dentist.repositories.ClinicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClinicService {
    @Autowired
    private ClinicRepository clinicRepository;

    public List<Clinic> findAll() {
        return clinicRepository.findAll();
    }

    public Clinic findClinicByAddress(String address) {
        return clinicRepository.findByAddress(address).orElseThrow(RuntimeException::new);

    }

    public Iterable<Maintenance> findMaintenancesByClinic(Long ClinicId) {
        return clinicRepository.findById(ClinicId).orElseThrow().getMaintenances();
    }
}
