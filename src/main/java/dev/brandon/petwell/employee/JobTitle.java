package dev.brandon.petwell.employee;

public enum JobTitle {

    VETERINARIAN("veterinarian"),
    VETERINARIAN_TECHNICIAN("veterinarian_technician"),
    VETERINARY_ASSISTANT("veterinary_assistant"),
    MANAGER("manager"),
    RECEPTIONIST("receptionist");

    private final String jobTitle;

    JobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobTitle() {
        return jobTitle;
    }
}
