package dev.brandon.petwell.employee;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum JobTitle {

    VETERINARIAN("veterinarian"),
    VETERINARIAN_TECHNICIAN("veterinarian_technician"),
    VETERINARY_ASSISTANT("veterinary_assistant"),
    MANAGER("manager"),
    RECEPTIONIST("receptionist");

    private final String jobTitle;
}
