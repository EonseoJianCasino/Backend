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


    public static AiRecommendationEntity create(Long ai_id, TestEntity id, Double improving_score, String improving_priority, String improving_title, String improving_content, String improving_effect, String related_metric){

        return AiRecommendationEntity.builder()
                .ai_id(ai_id)
                .id(id)
                .improving_score(improving_score)
                .improving_priority(improving_priority)
                .improving_title(improving_title)
                .improving_content(improving_content)
                .improving_effect(improving_effect)
                .related_metric(related_metric)
                .build();

    }



}


