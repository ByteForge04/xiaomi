package org.xiaomi.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void constructor_ValidNodeId() {
        assertDoesNotThrow(() -> new SnowflakeIdGenerator(5));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 1024})
    void constructor_InvalidNodeId(long nodeId) {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeIdGenerator(nodeId));
    }

    @Test
    void nextId_GeneratesUniqueIds() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1);
        long id1 = generator.nextId();
        long id2 = generator.nextId();
        
        assertNotEquals(id1, id2);
    }

    @Test
    void nextId_GeneratesIncreasingIds() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1);
        long id1 = generator.nextId();
        long id2 = generator.nextId();
        
        assertTrue(id2 > id1);
    }

    @Test
    void nextId_GeneratesIdsWithCorrectNodeId() {
        int nodeId = 5;
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(nodeId);
        long id = generator.nextId();
        
        // 提取节点ID部分（中间10位）
        long extractedNodeId = (id >> 12) & 0x3FF;
        assertEquals(nodeId, extractedNodeId);
    }
} 