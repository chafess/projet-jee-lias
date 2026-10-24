package com.fsbm.lias.repository;

import com.fsbm.lias.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Récupérer toutes les notifications d'un membre (triées par date)
    List<Notification> findByRecipientIdOrderByDateCreationDesc(Long recipientId);

    // Compter les notifications non lues (pour le badge rouge de la cloche)
    long countByRecipientIdAndIsReadFalse(Long recipientId);
}