package com.nsv.backend.chat.repository;

import com.nsv.backend.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Tin nhắn giữa hai người, cũ trước mới sau, chỉ lấy các tin có id lớn hơn afterId. */
    @Query("select m from Message m where "
            + "((m.senderId = :a and m.receiverId = :b) or (m.senderId = :b and m.receiverId = :a)) "
            + "and m.id > :afterId order by m.id asc")
    List<Message> findConversation(@Param("a") Long a, @Param("b") Long b,
            @Param("afterId") Long afterId, Pageable pageable);
}
