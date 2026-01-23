package com.example.joblink.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Post implements Serializable {
    private String id;
    private String employerId;
    private String title;
    private String jobDescription;
    private List<String> images;
    private int coverImageIndex = 0;
    private long timestamp;
    private String locationDescription;
    private String mapLink;
    private String province;
    private String daysPerWeek;
    private String startTime;
    private String endTime;
    private String salary;
    private String experience;
    private String workPlaceType;
    private double latitude;
    private double longitude;
    private boolean available = true;
    private String otherRequirements;
    private List<String> requirements;
    private String whyWorkHere;
    private String jobCategory;
    private String workType;
    private String industry;
    private String businessName;
    private String businessType;
    private String dayOff;
    private String phoneNumber;
    private String email;
    private String telegramLink;

    // Benefits
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

    // Amenities
    private boolean amenityFreeWifi;
    private boolean amenityRestArea;
    private boolean amenityFlexibleBreaks;
    private boolean amenityParkingSpot;
    private boolean amenityLocker;
    private boolean amenityEmployeeEvents;
    private boolean amenityAirConditioned;
    private boolean amenitySafetyEquipment;

    private boolean isBookmarked;

    public Post() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmployerId() { return employerId; }
    public void setEmployerId(String employerId) { this.employerId = employerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public int getCoverImageIndex() { return coverImageIndex; }
    public void setCoverImageIndex(int coverImageIndex) { this.coverImageIndex = coverImageIndex; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }

    public String getMapLink() { return mapLink; }
    public void setMapLink(String mapLink) { this.mapLink = mapLink; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(String daysPerWeek) { this.daysPerWeek = daysPerWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getWorkPlaceType() { return workPlaceType; }
    public void setWorkPlaceType(String workPlaceType) { this.workPlaceType = workPlaceType; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getOtherRequirements() { return otherRequirements; }
    public void setOtherRequirements(String otherRequirements) { this.otherRequirements = otherRequirements; }

    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }

    public String getWhyWorkHere() { return whyWorkHere; }
    public void setWhyWorkHere(String whyWorkHere) { this.whyWorkHere = whyWorkHere; }

    public String getJobCategory() { return jobCategory; }
    public void setJobCategory(String jobCategory) { this.jobCategory = jobCategory; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getDayOff() { return dayOff; }
    public void setDayOff(String dayOff) { this.dayOff = dayOff; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelegramLink() { return telegramLink; }
    public void setTelegramLink(String telegramLink) { this.telegramLink = telegramLink; }

    // Benefits
    public boolean isBenefitFreeMeal() { return benefitFreeMeal; }
    public void setBenefitFreeMeal(boolean benefitFreeMeal) { this.benefitFreeMeal = benefitFreeMeal; }
    public boolean isBenefitMonthlyBonus() { return benefitMonthlyBonus; }
    public void setBenefitMonthlyBonus(boolean benefitMonthlyBonus) { this.benefitMonthlyBonus = benefitMonthlyBonus; }
    public boolean isBenefitOvertimePay() { return benefitOvertimePay; }
    public void setBenefitOvertimePay(boolean benefitOvertimePay) { this.benefitOvertimePay = benefitOvertimePay; }
    public boolean isBenefitUniformProvided() { return benefitUniformProvided; }
    public void setBenefitUniformProvided(boolean benefitUniformProvided) { this.benefitUniformProvided = benefitUniformProvided; }
    public boolean isBenefitStaffDiscounts() { return benefitStaffDiscounts; }
    public void setBenefitStaffDiscounts(boolean benefitStaffDiscounts) { this.benefitStaffDiscounts = benefitStaffDiscounts; }
    public boolean isBenefitHealthInsurance() { return benefitHealthInsurance; }
    public void setBenefitHealthInsurance(boolean benefitHealthInsurance) { this.benefitHealthInsurance = benefitHealthInsurance; }
    public boolean isBenefitHoliday() { return benefitHoliday; }
    public void setBenefitHoliday(boolean benefitHoliday) { this.benefitHoliday = benefitHoliday; }
    public boolean isBenefitEndOfYearBonus() { return benefitEndOfYearBonus; }
    public void setBenefitEndOfYearBonus(boolean benefitEndOfYearBonus) { this.benefitEndOfYearBonus = benefitEndOfYearBonus; }
    public boolean isBenefitEquipmentProvided() { return benefitEquipmentProvided; }
    public void setBenefitEquipmentProvided(boolean benefitEquipmentProvided) { this.benefitEquipmentProvided = benefitEquipmentProvided; }
    public boolean isBenefitInternetAllowance() { return benefitInternetAllowance; }
    public void setBenefitInternetAllowance(boolean benefitInternetAllowance) { this.benefitInternetAllowance = benefitInternetAllowance; }
    public boolean isBenefitHotelProvided() { return benefitHotelProvided; }
    public void setBenefitHotelProvided(boolean benefitHotelProvided) { this.benefitHotelProvided = benefitHotelProvided; }
    public boolean isBenefitAccommodation() { return benefitAccommodation; }
    public void setBenefitAccommodation(boolean benefitAccommodation) { this.benefitAccommodation = benefitAccommodation; }
    public boolean isBenefitCertificate() { return benefitCertificate; }
    public void setBenefitCertificate(boolean benefitCertificate) { this.benefitCertificate = benefitCertificate; }
    public boolean isBenefitTransportProvided() { return benefitTransportProvided; }
    public void setBenefitTransportProvided(boolean benefitTransportProvided) { this.benefitTransportProvided = benefitTransportProvided; }

    // Amenities
    public boolean isAmenityFreeWifi() { return amenityFreeWifi; }
    public void setAmenityFreeWifi(boolean amenityFreeWifi) { this.amenityFreeWifi = amenityFreeWifi; }
    public boolean isAmenityRestArea() { return amenityRestArea; }
    public void setAmenityRestArea(boolean amenityRestArea) { this.amenityRestArea = amenityRestArea; }
    public boolean isAmenityFlexibleBreaks() { return amenityFlexibleBreaks; }
    public void setAmenityFlexibleBreaks(boolean amenityFlexibleBreaks) { this.amenityFlexibleBreaks = amenityFlexibleBreaks; }
    public boolean isAmenityParkingSpot() { return amenityParkingSpot; }
    public void setAmenityParkingSpot(boolean amenityParkingSpot) { this.amenityParkingSpot = amenityParkingSpot; }
    public boolean isAmenityLocker() { return amenityLocker; }
    public void setAmenityLocker(boolean amenityLocker) { this.amenityLocker = amenityLocker; }
    public boolean isAmenityEmployeeEvents() { return amenityEmployeeEvents; }
    public void setAmenityEmployeeEvents(boolean amenityEmployeeEvents) { this.amenityEmployeeEvents = amenityEmployeeEvents; }
    public boolean isAmenityAirConditioned() { return amenityAirConditioned; }
    public void setAmenityAirConditioned(boolean amenityAirConditioned) { this.amenityAirConditioned = amenityAirConditioned; }
    public boolean isAmenitySafetyEquipment() { return amenitySafetyEquipment; }
    public void setAmenitySafetyEquipment(boolean amenitySafetyEquipment) { this.amenitySafetyEquipment = amenitySafetyEquipment; }

    public boolean isBookmarked() { return isBookmarked; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    // Helper methods (Excluded from Firebase)
    @Exclude
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

    @Exclude
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

    // Compatibility methods (marked with @Exclude to avoid redundant database entries)
    @Exclude
    public String getPostId() { return id; }
    @Exclude
    public void setPostId(String postId) { this.id = postId; }
    @Exclude
    public String getDescription() { return jobDescription; }
    @Exclude
    public void setDescription(String description) { this.jobDescription = description; }
    @Exclude
    public String getAddressDetails() { return locationDescription; }
    @Exclude
    public void setAddressDetails(String addressDetails) { this.locationDescription = addressDetails; }
    @Exclude
    public String getMapsLink() { return mapLink; }
    @Exclude
    public void setMapsLink(String mapsLink) { this.mapLink = mapsLink; }
    @Exclude
    public String getLocation() { return province; }
    @Exclude
    public void setLocation(String location) { this.province = location; }
    @Exclude
    public String getWorkDays() { return daysPerWeek; }
    @Exclude
    public void setWorkDays(String workDays) { this.daysPerWeek = workDays; }
    @Exclude
    public String getExperienceLevel() { return experience; }
    @Exclude
    public void setExperienceLevel(String experienceLevel) { this.experience = experienceLevel; }
    @Exclude
    public String getWorkModel() { return workPlaceType; }
    @Exclude
    public void setWorkModel(String workModel) { this.workPlaceType = workModel; }
    
    @Exclude
    public String getWorkHours() {
        if (startTime != null && endTime != null) {
            return startTime + " - " + endTime;
        }
        return startTime != null ? startTime : "";
    }

    @Exclude
    public void setWorkHours(String workHours) {
        if (workHours != null && workHours.contains(" - ")) {
            String[] times = workHours.split(" - ");
            this.startTime = times.length > 0 ? times[0].trim() : null;
            this.endTime = times.length > 1 ? times[1].trim() : null;
        } else {
            this.startTime = workHours;
        }
    }
}
