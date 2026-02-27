import { describe, it, expect } from "vitest";
import { partitionsToBatchQueues, type PartitionInfo } from "@/lib/api/clusterInfo";

describe("partitionsToBatchQueues", () => {
  it("returns empty array when partitions is empty", () => {
    expect(partitionsToBatchQueues([])).toEqual([]);
  });

  it("returns empty array when partitions is null/undefined", () => {
    expect(partitionsToBatchQueues(null as unknown as PartitionInfo[])).toEqual([]);
    expect(partitionsToBatchQueues(undefined as unknown as PartitionInfo[])).toEqual([]);
  });

  it("maps a single partition to a batch queue with defaults", () => {
    const partitions: PartitionInfo[] = [
      {
        partitionName: "normal",
        nodeCount: 10,
        maxCpusPerNode: 24,
        maxGpusPerNode: 0,
        accounts: ["myaccount"],
      },
    ];
    const queues = partitionsToBatchQueues(partitions);
    expect(queues).toHaveLength(1);
    expect(queues[0]).toMatchObject({
      queueName: "normal",
      maxNodes: 10,
      cpuPerNode: 24,
      maxProcessors: 10 * 24,
      maxRunTime: 60,
      maxMemory: 0,
    });
  });

  it("maps multiple partitions preserving order", () => {
    const partitions: PartitionInfo[] = [
      { partitionName: "small", nodeCount: 2, maxCpusPerNode: 8, maxGpusPerNode: 0, accounts: [] },
      { partitionName: "gpu", nodeCount: 4, maxCpusPerNode: 16, maxGpusPerNode: 2, accounts: ["gpu-acct"] },
    ];
    const queues = partitionsToBatchQueues(partitions);
    expect(queues).toHaveLength(2);
    expect(queues[0].queueName).toBe("small");
    expect(queues[0].maxNodes).toBe(2);
    expect(queues[0].maxProcessors).toBe(16);
    expect(queues[1].queueName).toBe("gpu");
    expect(queues[1].maxNodes).toBe(4);
    expect(queues[1].cpuPerNode).toBe(16);
    expect(queues[1].maxProcessors).toBe(64);
  });

  it("handles missing numeric fields with 0", () => {
    const partitions: PartitionInfo[] = [
      {
        partitionName: "minimal",
        nodeCount: 0,
        maxCpusPerNode: 0,
        maxGpusPerNode: 0,
        accounts: [],
      },
    ];
    const queues = partitionsToBatchQueues(partitions);
    expect(queues[0]).toMatchObject({
      queueName: "minimal",
      maxNodes: 0,
      maxProcessors: 0,
      cpuPerNode: 0,
    });
  });
});
