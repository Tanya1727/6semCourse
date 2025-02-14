package com.clinic.dentist.controllers;

import com.clinic.dentist.api.service.IPatientService;
import com.clinic.dentist.date.DateSystem;
import com.clinic.dentist.date.TimeConverter;
import com.clinic.dentist.models.*;
import com.clinic.dentist.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class AdminController {
    @Autowired
    @Qualifier("patientService")
    private IPatientService patientService;
    @Autowired
    @Qualifier("appointmentService")
    private AppointmentService appointmentService;
    @Autowired
    @Qualifier("maintenanceService")
    private MaintenanceService maintenanceService;
    @Autowired
    @Qualifier("dentistService")
    private DentistService dentistService;
    @Autowired
    @Qualifier("clinicService")
    private ClinicService clinicService;
    @Autowired
    @Qualifier("typeServicesService")
    private TypeServicesService typeServicesService;

//    @GetMapping("/admin")
//    public String greeting(Model model) {
//        return "admin";
//    }

    @GetMapping("/admin/patients")
    public String getPatientView(Model model) {
        List<Patient> patients = patientService.getRegisteredPatients();
        model.addAttribute("patients", patients);
        return "patient";
    }

    @GetMapping("/admin/patients/register")
    public String getUnregisteredPatientView(Model model) {
        List<Patient> patients = patientService.getUnregisteredPatients();
        model.addAttribute("patients", patients);
        return "unregisteredPatients";
    }

    @GetMapping("/admin/patients/register/{id}")
    public String getUnregisteredPatientView(@PathVariable(value = "id") long id, Model model) {
        patientService.registeredPatient(id);
        List<Patient> patients = patientService.getUnregisteredPatients();
        model.addAttribute("patients", patients);
        return "unregisteredPatients";
    }

    @GetMapping("/admin/patients/{id}/orders")
    public String getPatientOrders(@PathVariable(value = "id") long id, Model model) {
        Patient patient = patientService.findById(id);
        List<Appointment> appointments = appointmentService.getAppointmentsWithActiveForPatient(patient.getId());
        Collections.reverse(appointments);
        model.addAttribute("patient", patient);

        model.addAttribute("orders", appointments);
        return "ordersPatients";
    }

    @GetMapping("/admin/patients/{id}/orders/remove/{id1}")
    public String getUnregisteredPatientView(@PathVariable(value = "id") long id, @PathVariable(value = "id1") long id1, Model model) {


        appointmentService.deleteAppointment(id1);
        Patient patient = patientService.findById(id);
        List<Appointment> appointments = appointmentService.getAppointmentsWithActiveForPatient(patient.getId());
        Collections.reverse(appointments);
        model.addAttribute("orders", appointments);
        model.addAttribute("patient", patient);
        return "ordersPatients";

    }

    @GetMapping("/admin/services")
    public String getService(Model model) {
        List<Maintenance> maintenances = maintenanceService.sortByName();
        model.addAttribute("services", maintenances);
        return ("servicesAdmin");

    }

    @GetMapping("/admin/dentists")
    public String showDentists(Model model) {
        List<Dentist> dentists = dentistService.sortbyAlphabet();
        model.addAttribute("dentists", dentists);
        return ("doctors");
    }

    @GetMapping("/admin/dentists/{id}/remove")
    public String removeDentist(@PathVariable(value = "id") long id, Model model) {
        if (!dentistService.checkExist(id)) {
            return "redirect:/admin/dentists";
        }
        boolean delete = dentistService.deleteEntity(id);
        if (!delete) {
            return "redirect:/admin/dentists/" + id + "/remove";
        }

        return "redirect:/admin/dentists";

    }

    private Dentist den;

    @PostMapping("/admin/dentists/{id}/appointments")
    public String getAppointmentDentistDate(@PathVariable(value = "id") long id, Model model, @RequestParam String Date) {
        if (!dentistService.checkExist(id)) {
            return "redirect:/admin/dentists";
        }

        List<Appointment> appointments = null;
        Dentist dentist = dentistService.findById(id);
        model.addAttribute(dentist);

        if (DateSystem.checkWeekend(Date)) {
            appointments = appointmentService.getActualAppointmentsForDoctor(dentist, DateSystem.NextDay());
            model.addAttribute("dates", DateSystem.NextDay());
            model.addAttribute("norm_date", TimeConverter.getDate2(DateSystem.NextDay()));

        } else {
            appointments = appointmentService.getActualAppointmentsForDoctor(dentist, Date);
            model.addAttribute("dates", Date);
            model.addAttribute("norm_date", Date);


        }
        Collections.sort(appointments);

        model.addAttribute("orders", appointments);
        return "adminDentistAppointment";

    }


    @GetMapping("/admin/dentists/{id}/appointments")
    public String getAppointment(@PathVariable(value = "id") long id, Model model) {
        if (!dentistService.checkExist(id)) {
            return "redirect:/admin/dentists";
        }
        Date thisDay = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd");
        String dat = formatForDateNow.format(thisDay);
        List<Appointment> appointments;
        Dentist dentist = dentistService.findById(id);
        model.addAttribute(dentist);
        if (DateSystem.checkWeekend(dat)) {
            appointments = appointmentService.getActualAppointmentsForDoctor(dentist, DateSystem.NextDay());
            model.addAttribute("dates", DateSystem.NextDay());
            model.addAttribute("norm_date", TimeConverter.getDate2(DateSystem.NextDay()));

        } else {
            appointments = appointmentService.getActualAppointmentsForDoctor(dentist, dat);
            model.addAttribute("dates", dat);
            model.addAttribute("norm_date", dat);

        }
        Collections.sort(appointments);
        model.addAttribute("orders", appointments);

        return "adminDentistAppointment";

    }

    @GetMapping("/admin/{id}/dentist/add")
    public String createDentist(@PathVariable(value = "id") long id, Model model) {
        model.addAttribute("dentist", new Dentist());
        model.addAttribute("clinicId", id);

        return "createDentist";
    }

    @PostMapping("/admin/{id}/dentist/add")
    public String getDentist(@ModelAttribute(name = "dentist") Dentist dentist, @PathVariable(value = "id") long id, Model model) {
        den = null;
        if (dentist.getFirstName().trim().equals("")) {
            model.addAttribute("firstNameError", "Имя не введено");
            model.addAttribute("clinicId", id);

            return "createDentist";
        } else if (dentist.getLastName().trim().equals("")) {

            model.addAttribute("lastNameError", "Фамилия не введена");
            model.addAttribute("clinicId", id);

            return "createDentist";
        } else if (dentist.getPatronymic().trim().equals("")) {
            model.addAttribute("patronymicError", "Отчество не введено");
            model.addAttribute("clinicId", id);

            return "createDentist";
        } else if (dentist.getPhoneNumber().trim().equals("")) {
            model.addAttribute("clinicId", id);

            model.addAttribute("phoneNumberError", "Номер телефона не введен");
            return "createDentist";
        }

        Pattern pattern = Pattern.compile("^(375)[0-9]{9}$");
        Matcher matcher = pattern.matcher(dentist.getPhoneNumber().trim());
        if (!matcher.matches()) {
            model.addAttribute("clinicId", id);

            model.addAttribute("phoneNumberError", "Номер телефона введен не корректно");

            return "createDentist";
        } else if (dentistService.findDentistByPhoneNumber(dentist.getPhoneNumber().trim())) {

            model.addAttribute("phoneNumberError", "Врач с данным номером телефона зарегистрирован");
            model.addAttribute("clinicId", id);

            return "createDentist";
        }
        try {

            dentist.setFirstName(dentist.getFirstName().trim());
            dentist.setLastName(dentist.getLastName().trim());
            dentist.setPhoneNumber(dentist.getPhoneNumber().trim());
            dentist.setPatronymic(dentist.getPatronymic().trim());
            dentist.setClinic(clinicService.findById(id));
        } catch (RuntimeException ex) {
            model.addAttribute("clinicId", id);

            return "createDentist";

        }

        den = dentist;
        return "redirect:/admin/{id}/dentist/add/services";
    }

    @GetMapping("/admin/{id}/dentist/add/services")
    public String getServices(Model model, @PathVariable(value = "id") long id) {
        Iterable<Maintenance> maintenanceList = clinicService.findMaintenancesByClinic(den.getClinic().getId());
        model.addAttribute("services", maintenanceList);
        model.addAttribute("clinicId", id);

        if (den != null) {
            model.addAttribute("dentist", den);
        }
        return "chooseServicesForDentist";

    }

    @PostMapping("/admin/{id}/dentist/add/services")
    public String finishCreateDentist1(@RequestParam(required = false) String[] services, @PathVariable(value = "id") long id, Model model) {
        if (services == null) {
            Iterable<Maintenance> maintenanceList = clinicService.findMaintenancesByClinic(den.getClinic().getId());
            model.addAttribute("services", maintenanceList);
            model.addAttribute("servicesError", "Услуги не выбраны");
            model.addAttribute("dentist", den);
            model.addAttribute("clinicId", id);

            return "chooseServicesForDentist";
        }
        den.setMaintenances(maintenanceService.getSetFromArrayMaintenance(services));
        dentistService.addEntity(den);
        den = null;
        return "redirect:/admin";

    }

    @GetMapping("/admin/services/add")
    public String createService(Model model) {
        List<Clinic> clinics = clinicService.findAll();
        List<TypeServices> typeServices = typeServicesService.getAll();
        model.addAttribute("service", new Maintenance());
        return "createMaintenance";
    }

    @PostMapping("/admin/services/add")
    public String getService(@ModelAttribute(name = "service") Maintenance service, Model model) {

//        if (service.getName().trim().equals("")) {
//            model.addAttribute("nameError", "Название не введено");
//
//            return "createService";
//        } else if (service.getDescription().trim().equals("")) {
//
//            model.addAttribute("descriptionError", "Описание не введено");
//
//            return "createService";
//        }
        service.setName(service.getName().trim());
        service.setDescription(service.getDescription().trim());

        if (maintenanceService.checkHaveThisMaintenance(service)) {
            model.addAttribute("nameError", "Услуга с таким названием уже существует");
        }
        maintenanceService.addMaintenance(service);
        return "redirect:/admin/services";
    }

    @GetMapping("/admin")
    public String getClinics(Model model) {
        List<Clinic> clinics = clinicService.findAll();
        model.addAttribute("clinics", clinics);
        return "admin";
    }

    @GetMapping("/admin/{id}/dentists")
    public String getDentistsClinics(@PathVariable(value = "id") long id, Model model) {
        List<Dentist> dentists = clinicService.findDentistsByClinic(id);
        model.addAttribute("dentists", dentists);
        return ("doctors");
    }

    @GetMapping("/admin/{id}/services")
    public String getServicesClinics(@PathVariable(value = "id") long id, Model model) {
        Iterable<Maintenance> services = clinicService.findMaintenancesByClinic(id);
        model.addAttribute("services", services);
        return ("servicesAdmin");
    }

    @PostMapping("/admin/{id}/appointments")
    public String getAppointmentClinicDate(@PathVariable(value = "id") long id, Model model, @RequestParam String Date) {
        if (!clinicService.checkExist(id)) {
            return "redirect:/admin";
        }

        List<Appointment> appointments = null;
        Clinic clinic = clinicService.findById(id);
        model.addAttribute(clinic);

        if (DateSystem.checkWeekend(Date)) {
            appointments = appointmentService.getActualAppointmentsForClinic(clinic, DateSystem.NextDay());
            model.addAttribute("dates", DateSystem.NextDay());
            model.addAttribute("norm_date", TimeConverter.getDate2(DateSystem.NextDay()));

        } else {
            appointments = appointmentService.getActualAppointmentsForClinic(clinic, Date);
            model.addAttribute("dates", Date);
            model.addAttribute("norm_date", Date);


        }
        Collections.sort(appointments);
        model.addAttribute("id", id);
        model.addAttribute("orders", appointments);
        return "clinicAppointment";

    }

    @PostMapping("/admin/patients/{id}/edit")
    public String clientEdit2(@PathVariable(value = "id") long id, @RequestParam String Username, Model model) {

        Patient patient = patientService.findById(id);


        if (Username.trim().equals("")) {
            model.addAttribute(patient);
            model.addAttribute("phoneError", "Новый номер не введен");
            return ("changeNumber");
        }

        Pattern pattern = Pattern.compile("^(375)[0-9]{9}$");
        Matcher matcher = pattern.matcher(Username.trim());
        if (!matcher.matches()) {
            model.addAttribute(patient);
            model.addAttribute("phoneError", "Номер телефона введен не корректно");
            return ("changeNumber");

        }
        if (patientService.checkPatient(Username.trim())) {
            model.addAttribute(patient);
            model.addAttribute("phoneError", "Данный номер занят");
            return ("changeNumber");
        }
        patient.setUsername(Username.trim());
        patientService.save(patient);

        return "redirect:/admin/patients";
    }


    @GetMapping("/admin/{id}/appointments")
    public String getAppointmentForClinic(@PathVariable(value = "id") long id, Model model) {
        if (!clinicService.checkExist(id)) {
            return "redirect:/admin";
        }
        Date thisDay = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd");
        String dat = formatForDateNow.format(thisDay);
        List<Appointment> appointments;
        Clinic clinic = clinicService.findById(id);
        model.addAttribute(clinic);
        if (DateSystem.checkWeekend(dat)) {
            appointments = appointmentService.getActualAppointmentsForClinic(clinic, DateSystem.NextDay());
            model.addAttribute("dates", DateSystem.NextDay());
            model.addAttribute("norm_date", TimeConverter.getDate2(DateSystem.NextDay()));

        } else {
            appointments = appointmentService.getActualAppointmentsForClinic(clinic, dat);
            model.addAttribute("dates", dat);
            model.addAttribute("norm_date", dat);

        }
        Collections.sort(appointments);
        model.addAttribute("id", id);

        model.addAttribute("orders", appointments);

        return "clinicAppointment";

    }

    @GetMapping("/admin/patients/{id}/edit")
    public String showTemplateToEditPatientPhoneNumber(@PathVariable(value = "id") long id, Model model) {
        Patient patient = patientService.findById(id);
        model.addAttribute("patient", patient);
        return "changeNumber";
    }
    @GetMapping("/admin/patients/register/{id}/delete")
    public String deletePatient(@PathVariable(value = "id") long id, Model model) {
        if (!patientService.checkExist(id)) {
            return "redirect:/admin/patients/register";
        }

        Patient patient = patientService.findById(id);
        patientService.delete(patient);
         return "redirect:/admin/patients/register";
    }

}
