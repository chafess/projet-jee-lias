package com.fsbm.lias.repository;

import com.fsbm.lias.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> { }