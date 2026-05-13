package com.hastane.service;

import com.hastane.model.Branch;
import com.hastane.model.DoctorBranchView;
import com.hastane.repository.BranchRepository;
import com.hastane.repository.DoctorRepository;

import java.util.List;

public class DoctorService {
    private final DoctorRepository doctorRepository = new DoctorRepository();
    private final BranchRepository branchRepository = new BranchRepository();

    public void registerNewDoctor(String name, int branchId) {

        if (name == null || name.trim().isEmpty()) {
            System.out.println("❌ Hata: Doktor ismi boş olamaz!");
            return;
        }

        if (!branchExists(branchId)) {
            System.out.println("❌ Hata: Geçersiz branş ID.");
            return;
        }

        doctorRepository.addDoctor(name, branchId);
    }

    public void showAllDoctors() {
        List<DoctorBranchView> doctors = doctorRepository.listDoctorsWithBranches();
        printDoctorTable(doctors, "Mevcut Doktorlar");
    }

    public void searchDoctorsByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("⚠️ Hata: Arama kelimesi boş olamaz.");
            return;
        }
        List<DoctorBranchView> doctors = doctorRepository.searchDoctorsByName(keyword.trim());
        printDoctorTable(doctors, "Arama Sonucu");
    }

    public void showDoctorsByBranch(int branchId) {
        if (!branchExists(branchId)) {
            System.out.println("❌ Hata: Geçersiz branş ID.");
            return;
        }
        List<DoctorBranchView> doctors = doctorRepository.findDoctorsByBranchId(branchId);
        printDoctorTable(doctors, "Branşa Göre Doktorlar");
    }

    public void showAllBranches() {
        List<Branch> branches = branchRepository.getAllBranches();
        if (branches.isEmpty()) {
            System.out.println("ℹ️ Kayıtlı branş bulunamadı.");
            return;
        }

        System.out.println("\n----- Branşlar -----");
        for (Branch branch : branches) {
            System.out.println("ID: " + branch.getId() + " | Ad: " + branch.getName());
        }
    }

    public void registerNewBranch(String branchName) {
        if (branchName == null || branchName.trim().isEmpty()) {
            System.out.println("❌ Hata: Branş adı boş olamaz.");
            return;
        }

        String normalizedName = branchName.trim();
        List<Branch> branches = branchRepository.getAllBranches();
        for (Branch branch : branches) {
            if (branch.getName() != null && branch.getName().equalsIgnoreCase(normalizedName)) {
                System.out.println("⚠️ Bu branş zaten mevcut: " + normalizedName);
                return;
            }
        }

        branchRepository.addBranch(normalizedName);
    }

    private boolean branchExists(int branchId) {
        List<Branch> branches = branchRepository.getAllBranches();
        for (Branch branch : branches) {
            if (branch.getId() == branchId) {
                return true;
            }
        }
        return false;
    }

    private void printDoctorTable(List<DoctorBranchView> doctors, String title) {
        if (doctors.isEmpty()) {
            System.out.println("ℹ️ " + title + " için kayıt bulunamadı.");
            return;
        }

        System.out.println("\n----- " + title + " -----");
        System.out.printf("%-6s %-25s %-10s %-20s%n", "ID", "Doktor", "Branş ID", "Branş");
        System.out.println("---------------------------------------------------------------");

        for (DoctorBranchView doctor : doctors) {
            System.out.printf(
                    "%-6d %-25s %-10d %-20s%n",
                    doctor.getDoctorId(),
                    doctor.getDoctorName(),
                    doctor.getBranchId(),
                    doctor.getBranchName()
            );
        }
    }
}