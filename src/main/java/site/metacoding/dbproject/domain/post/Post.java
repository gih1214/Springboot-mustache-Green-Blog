package site.metacoding.dbproject.domain.post;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import site.metacoding.dbproject.domain.user.User;

// JPA 라이브러리는 Java(자바언어) Persistence(DB에 영구적인 저장) API(노출되어 있는 메서드=인터페이스)
// 즉, JPA는 자바언어로 디비에 데이터를 저장하게 해주는 메서드를 가지고 있음
// 1. CRUD 메서드를 기본 제공
// 2. 자바코드로 DB를 자동 생성해주는 기능 제공 - User
// 3. ORM 제공!! - 이 부분 지금 몰라도 됨. - 이거해보자!! (Object Relation Mapping)

@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 300, nullable = false)
    private String title;

    @Lob // CLOB 4GB 문자 타입
    @Column(nullable = false)
    private String content;

    @JoinColumn(name = "userId")
    @ManyToOne
    private User user;

    private LocalDateTime createDate;
}
