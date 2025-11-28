package com.example.joblink.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Post implements Serializable {


    private double latitude;
    private double longitude;
    private boolean available = true;
    private String postId;
    private String employerId;
    private String title;
    private String description;
    private List<String> images;
    private long timestamp;
    private String businessName;
    private String location;
    private String mapsLink;
    private String addressDetails;
    private String workModel;
    private String workHours;
    private String workDays;
    private String dayOff;
    private String salary;
    private boolean benefitFreeMeal;
    private boolean benefitMonthlyBonus;
    private boolean benefitOvertimePay;
    private boolean benefitUniformProvided;
    private boolean benefitStaffDiscounts;
    private boolean benefitHealthInsurance;
    private boolean benefitHoliday;
    private boolean benefitEndOfYearBonus;
    private boolean benefitEquipmentProvided;
    private boolean benefitInternetAllowance;
    private boolean benefitHotelProvided;
    private boolean benefitAccommodation;
    private boolean benefitCertificate;
    private boolean benefitTransportProvided;
    private boolean amenityFreeWifi;
    private boolean amenityRestArea;
    private boolean amenityFlexibleBreaks;
    private boolean amenityParkingSpot;
    private boolean amenityLocker;
    private boolean amenityEmployeeEvents;
    private boolean amenityAirConditioned;
    private boolean amenitySafetyEquipment;
    private boolean isBookmarked;
    private List<String> requirements;
    private String whyWorkHere;
    private String jobCategory;
    private String workType;
    private String industry;
    private String experienceLevel;

    public Post() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMapsLink() {
        return mapsLink;
    }

    public void setMapsLink(String mapsLink) {
        this.mapsLink = mapsLink;
    }

    public String getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(String addressDetails) {
        this.addressDetails = addressDetails;
    }

    public String getWorkModel() {
        return workModel;
    }

    public void setWorkModel(String workModel) {
        this.workModel = workModel;
    }

    public String getWorkHours() {
        return workHours;
    }

    public void setWorkHours(String workHours) {
        this.workHours = workHours;
    }

    public String getWorkDays() {
        return workDays;
    }

    public void setWorkDays(String workDays) {
        this.workDays = workDays;
    }

    public String getDayOff() {
        return dayOff;
    }

    public void setDayOff(String dayOff) {
        this.dayOff = dayOff;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public String getWhyWorkHere() {
        return whyWorkHere;
    }

    public void setWhyWorkHere(String whyWorkHere) {
        this.whyWorkHere = whyWorkHere;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public boolean isBenefitFreeMeal() {
        return benefitFreeMeal;
    }

    public void setBenefitFreeMeal(boolean benefitFreeMeal) {
        this.benefitFreeMeal = benefitFreeMeal;
    }

    public boolean isBenefitMonthlyBonus() {
        return benefitMonthlyBonus;
    }

    public void setBenefitMonthlyBonus(boolean benefitMonthlyBonus) {
        this.benefitMonthlyBonus = benefitMonthlyBonus;
    }

    public boolean isBenefitOvertimePay() {
        return benefitOvertimePay;
    }

    public void setBenefitOvertimePay(boolean benefitOvertimePay) {
        this.benefitOvertimePay = benefitOvertimePay;
    }

    public boolean isBenefitUniformProvided() {
        return benefitUniformProvided;
    }

    public void setBenefitUniformProvided(boolean benefitUniformProvided) {
        this.benefitUniformProvided = benefitUniformProvided;
    }

    public boolean isBenefitStaffDiscounts() {
        return benefitStaffDiscounts;
    }

    public void setBenefitStaffDiscounts(boolean benefitStaffDiscounts) {
        this.benefitStaffDiscounts = benefitStaffDiscounts;
    }

    public boolean isBenefitHealthInsurance() {
        return benefitHealthInsurance;
    }

    public void setBenefitHealthInsurance(boolean benefitHealthInsurance) {
        this.benefitHealthInsurance = benefitHealthInsurance;
    }

    public boolean isBenefitHoliday() {
        return benefitHoliday;
    }

    public void setBenefitHoliday(boolean benefitHoliday) {
        this.benefitHoliday = benefitHoliday;
    }

    public boolean isBenefitEndOfYearBonus() {
        return benefitEndOfYearBonus;
    }

    public void setBenefitEndOfYearBonus(boolean benefitEndOfYearBonus) {
        this.benefitEndOfYearBonus = benefitEndOfYearBonus;
    }

    public boolean isBenefitEquipmentProvided() {
        return benefitEquipmentProvided;
    }

    public void setBenefitEquipmentProvided(boolean benefitEquipmentProvided) {
        this.benefitEquipmentProvided = benefitEquipmentProvided;
    }

    public boolean isBenefitInternetAllowance() {
        return benefitInternetAllowance;
    }

    public void setBenefitInternetAllowance(boolean benefitInternetAllowance) {
        this.benefitInternetAllowance = benefitInternetAllowance;
    }

    public boolean isBenefitHotelProvided() {
        return benefitHotelProvided;
    }

    public void setBenefitHotelProvided(boolean benefitHotelProvided) {
        this.benefitHotelProvided = benefitHotelProvided;
    }

    public boolean isBenefitAccommodation() {
        return benefitAccommodation;
    }

    public void setBenefitAccommodation(boolean benefitAccommodation) {
        this.benefitAccommodation = benefitAccommodation;
    }

    public boolean isBenefitCertificate() {
        return benefitCertificate;
    }

    public void setBenefitCertificate(boolean benefitCertificate) {
        this.benefitCertificate = benefitCertificate;
    }

    public boolean isBenefitTransportProvided() {
        return benefitTransportProvided;
    }

    public void setBenefitTransportProvided(boolean benefitTransportProvided) {
        this.benefitTransportProvided = benefitTransportProvided;
    }

    public boolean isAmenityFreeWifi() {
        return amenityFreeWifi;
    }

    public void setAmenityFreeWifi(boolean amenityFreeWifi) {
        this.amenityFreeWifi = amenityFreeWifi;
    }

    public boolean isAmenityRestArea() {
        return amenityRestArea;
    }

    public void setAmenityRestArea(boolean amenityRestArea) {
        this.amenityRestArea = amenityRestArea;
    }

    public boolean isAmenityFlexibleBreaks() {
        return amenityFlexibleBreaks;
    }

    public void setAmenityFlexibleBreaks(boolean amenityFlexibleBreaks) {
        this.amenityFlexibleBreaks = amenityFlexibleBreaks;
    }

    public boolean isAmenityParkingSpot() {
        return amenityParkingSpot;
    }

    public void setAmenityParkingSpot(boolean amenityParkingSpot) {
        this.amenityParkingSpot = amenityParkingSpot;
    }

    public boolean isAmenityLocker() {
        return amenityLocker;
    }

    public void setAmenityLocker(boolean amenityLocker) {
        this.amenityLocker = amenityLocker;
    }

    public boolean isAmenityEmployeeEvents() {
        return amenityEmployeeEvents;
    }

    public void setAmenityEmployeeEvents(boolean amenityEmployeeEvents) {
        this.amenityEmployeeEvents = amenityEmployeeEvents;
    }

    public boolean isAmenityAirConditioned() {
        return amenityAirConditioned;
    }

    public void setAmenityAirConditioned(boolean amenityAirConditioned) {
        this.amenityAirConditioned = amenityAirConditioned;
    }

    public boolean isAmenitySafetyEquipment() {
        return amenitySafetyEquipment;
    }

    public void setAmenitySafetyEquipment(boolean amenitySafetyEquipment) {
        this.amenitySafetyEquipment = amenitySafetyEquipment;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public List<String> getSelectedBenefits() {
        List<String> selectedBenefits = new ArrayList<>();
        if (benefitFreeMeal) selectedBenefits.add("Free meal");
        if (benefitMonthlyBonus) selectedBenefits.add("Monthly bonus");
        if (benefitOvertimePay) selectedBenefits.add("Overtime pay");
        if (benefitUniformProvided) selectedBenefits.add("Uniform provided");
        if (benefitStaffDiscounts) selectedBenefits.add("Staff discounts");
        if (benefitHealthInsurance) selectedBenefits.add("Health insurance");
        if (benefitHoliday) selectedBenefits.add("Holiday");
        if (benefitEndOfYearBonus) selectedBenefits.add("End-of-year bonus");
        if (benefitEquipmentProvided) selectedBenefits.add("Equipment provided");
        if (benefitInternetAllowance) selectedBenefits.add("Internet allowance");
        if (benefitHotelProvided) selectedBenefits.add("Hotel provided");
        if (benefitAccommodation) selectedBenefits.add("Accommodation");
        if (benefitCertificate) selectedBenefits.add("Certificate");
        if (benefitTransportProvided) selectedBenefits.add("Transport provided");
        return selectedBenefits;
    }

    public List<String> getSelectedAmenities() {
        List<String> selectedAmenities = new ArrayList<>();
        if (amenityFreeWifi) selectedAmenities.add("Free wifi");
        if (amenityRestArea) selectedAmenities.add("Rest area");
        if (amenityFlexibleBreaks) selectedAmenities.add("Flexible breaks");
        if (amenityParkingSpot) selectedAmenities.add("Parking spot");
        if (amenityLocker) selectedAmenities.add("Locker");
        if (amenityEmployeeEvents) selectedAmenities.add("Employee events");
        if (amenityAirConditioned) selectedAmenities.add("Air-conditioned");
        if (amenitySafetyEquipment) selectedAmenities.add("Safety equipment");
        return selectedAmenities;
    }

    public void setBenefitsFromList(List<String> benefitsList) {
        if (benefitsList == null) return;
        benefitFreeMeal = benefitsList.contains("Free meal");
        benefitMonthlyBonus = benefitsList.contains("Monthly bonus");
        benefitOvertimePay = benefitsList.contains("Overtime pay");
        benefitUniformProvided = benefitsList.contains("Uniform provided");
        benefitStaffDiscounts = benefitsList.contains("Staff discounts");
        benefitHealthInsurance = benefitsList.contains("Health insurance");
        benefitHoliday = benefitsList.contains("Holiday");
        benefitEndOfYearBonus = benefitsList.contains("End-of-year bonus");
        benefitEquipmentProvided = benefitsList.contains("Equipment provided");
        benefitInternetAllowance = benefitsList.contains("Internet allowance");
        benefitHotelProvided = benefitsList.contains("Hotel provided");
        benefitAccommodation = benefitsList.contains("Accommodation");
        benefitCertificate = benefitsList.contains("Certificate");
        benefitTransportProvided = benefitsList.contains("Transport provided");
    }

    public void setAmenitiesFromList(List<String> amenitiesList) {
        if (amenitiesList == null) return;
        amenityFreeWifi = amenitiesList.contains("Free wifi");
        amenityRestArea = amenitiesList.contains("Rest area");
        amenityFlexibleBreaks = amenitiesList.contains("Flexible breaks");
        amenityParkingSpot = amenitiesList.contains("Parking spot");
        amenityLocker = amenitiesList.contains("Locker");
        amenityEmployeeEvents = amenitiesList.contains("Employee events");
        amenityAirConditioned = amenitiesList.contains("Air-conditioned");
        amenitySafetyEquipment = amenitiesList.contains("Safety equipment");
    }

}
