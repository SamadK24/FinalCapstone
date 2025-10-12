package com.aurionpro.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "concern_attachment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcernAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long concernId;

    @Column(nullable = false, length = 300)
    private String ref;  // Cloudinary secure_url
}

