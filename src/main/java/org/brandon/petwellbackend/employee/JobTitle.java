package org.brandon.petwellbackend.employee;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobTitle {

    VETERINARIAN("Veterinarian"),
    VETERINARIAN_TECHNICIAN("Veterinarian Technician"),
    VETERINARY_ASSISTANT("Veterinary Assistant"),
    MANAGER("Manager");

    private final String title;
}
