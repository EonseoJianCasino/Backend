package com.test.webtest.domain.airecommendation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import com.test.webtest.domain.test.entity.TestEntity;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name="ai_recommendation")

public class AiRecommendationEntity {

    //필수 식별값들
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ai_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable=false)
    private TestEntity id;

    @Column(nullable=false)
    private Double improving_score;

    @Column(nullable=false)
    private String improving_priority;

    @Column(nullable=false)
    private String improving_title;

    @Column(nullable=false)
    private String improving_content;

    @Column(nullable=false)
    private String improving_effect;

    @Column(nullable=false)
    private String related_metric;


}


