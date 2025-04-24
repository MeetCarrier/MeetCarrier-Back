package com.kslj.mannam.block;

import com.kslj.mannam.TestUtils;
import com.kslj.mannam.domain.block.dto.BlockDto;
import com.kslj.mannam.domain.block.entity.Block;
import com.kslj.mannam.domain.block.service.BlockService;
import com.kslj.mannam.domain.user.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
public class BlockTest {

    @Autowired
    private BlockService blockService;

    @Autowired
    private TestUtils testUtils;

    // 블락 데이터 생성 메서드
    private BlockDto createBlockRequestDto(String BlockedPhone, String BlockedInfo) {
        return BlockDto.builder()
                .blockedPhone(BlockedPhone)
                .blockedInfo(BlockedInfo)
                .build();
    }

    // 새로운 블락 데이터 추가 및 조회 테스트
    @Test
    public void createBlockAndGetTest() {
        // given
        User testUser = testUtils.createAndGetTestUser();
        BlockDto testDto = createBlockRequestDto("010-1234-1234", "테스트 번호");

        // when
        for (int i=0; i<5; i++)
            blockService.createBlock(testUser, testDto);
        List<BlockDto> blocks = blockService.getBlocks(testUser);

        // then
        for(BlockDto block : blocks)
            System.out.println(block.getBlockedPhone() + ", " + block.getBlockedInfo());
        Assertions.assertThat(blocks.size()).isEqualTo(5);
    }

    // 블락 데이터 수정 테스트
    @Test
    public void updateBlockTest() {
        // given
        User testUser = testUtils.createAndGetTestUser();
        BlockDto testDto = createBlockRequestDto("010-1234-1234", "테스트 번호");

        long blockId = blockService.createBlock(testUser, testDto);

        // when
        BlockDto updateDto = createBlockRequestDto("010-7484-5883", "업데이트 번호");
        blockService.updateBlock(blockId, updateDto);

        BlockDto updatedBlock = blockService.getBlocks(testUser).get(0);

        // then
        System.out.println(updatedBlock.getBlockedPhone() + ", " + updatedBlock.getBlockedInfo());
        Assertions.assertThat(updatedBlock.getBlockedPhone()).isEqualTo(updateDto.getBlockedPhone());
    }


    // 블락 데이터 삭제 테스트
    @Test
    public void DeleteBlockTest() {
        // given
        User testUser = testUtils.createAndGetTestUser();
        BlockDto testDto = createBlockRequestDto("010-1234-1234", "테스트 번호");

        // when
        long blockId = blockService.createBlock(testUser, testDto);

        for (int i=0; i<4; i++)
            blockService.createBlock(testUser, testDto);

        blockService.deleteBlock(blockId);
        List<BlockDto> blocks = blockService.getBlocks(testUser);

        // then
        Assertions.assertThat(blocks.size()).isEqualTo(4);
    }
}
