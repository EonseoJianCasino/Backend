package com.test.webtest.domain.ai.entity; //.airecommendationentity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;


//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;


//@Getter @Setter
//@NoArgsConstructor @AllArgsConstructor
//@Builder


@Entity
@Table(name="ai_recommendations")
public class AiRecommendation {

    //필수 식별값들
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id; //ai_id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "test_id", nullable=false)
//    private TestEntity id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(nullable = false, length = 20) // PERF / SEC
    private String type;


    @Column(nullable = false, length = 200)
    private String title;

    @Column
    private String summaryOfImpr;

    @Column
    private String scoreImpr;                 // LCP, CLS, ...

    @Column
    private String relatedMetric;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;


    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static AiRecommendation of(UUID testId, String type, String metric, String title, String content) {
        AiRecommendation r = new AiRecommendation();
        r.id = UUID.randomUUID();
        r.testId = testId;
        r.type = type; // ??
        r.metric = metric;
        r.title = title;
        r.content = content;
        return r;

    }



//    @Column(nullable=false)
//    private Double improving_score;
//
//    @Column(nullable=false)
//    private String improving_priority;
//
//    @Column(nullable=false)
//    private String improving_title;
//
//    @Column(nullable=false)
//    private String improving_content;
//
//    @Column(nullable=false)
//    private String improving_effect;
//
//    @Column(nullable=false)
//    private String related_metric;
//
//
//    public static AiRecommendation create(Long ai_id, TestEntity id, Double improving_score, String improving_priority, String improving_title, String improving_content, String improving_effect, String related_metric){
//
//        return AiRecommendation.builder()
//                .ai_id(ai_id)
//                .id(id)
//                .improving_score(improving_score)
//                .improving_priority(improving_priority)
//                .improving_title(improving_title)
//                .improving_content(improving_content)
//                .improving_effect(improving_effect)
//                .related_metric(related_metric)
//                .build();
//
//    }
//


}


