package com.fsbm.lias.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DiscussionMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "discussion_id")
    private Discussion discussion;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Member author;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime dateEnvoi;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Discussion getDiscussion() { return discussion; }
    public void setDiscussion(Discussion discussion) { this.discussion = discussion; }
    public Member getAuthor() { return author; }
    public void setAuthor(Member author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
}