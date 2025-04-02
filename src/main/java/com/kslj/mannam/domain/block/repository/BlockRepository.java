package com.kslj.mannam.domain.block.repository;

import com.kslj.mannam.domain.block.entity.Block;
import com.kslj.mannam.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    List<Block> getBlockByUser(User user);
}
