package com.kslj.mannam.domain.block.repository;

import com.kslj.mannam.domain.block.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
}
