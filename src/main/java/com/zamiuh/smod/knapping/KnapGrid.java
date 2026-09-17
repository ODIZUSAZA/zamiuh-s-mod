package com.zamiuh.smod.knapping;

import net.minecraft.util.RandomSource;

/**
 * 打制网格（docs/13 v0.1.1）：32×32 方格代表待打制石头的剖面。
 * - 生成时按「缺陷与结构」在外围 3 层随机缺失方格（越外侧越容易缺失，禁止出现封闭孔洞）；
 * - 打击时以目标格为中心，在 5×5 范围内按力量 / 锋利度移除方格；
 * - 成品属性：打制度（缺失比例）、尖锐度（左右上三面轮廓峰值检测）。
 */
public class KnapGrid {
    public static final int SIZE = 32;
    public static final int CELLS = SIZE * SIZE;

    private final boolean[] filled = new boolean[CELLS];
    private int initialFilled = CELLS;
    private int strikeCount = 0;

    // ==================== 初始化 ====================

    /** 满格初始化并按缺陷值生成外围缺失（d：缺陷与结构 0~100）。 */
    public void generate(int defects, RandomSource random) {
        java.util.Arrays.fill(filled, true);
        strikeCount = 0;
        // 越外侧越容易缺失：最外层基准概率随缺陷值上升
        double base = 0.08 + defects * 0.004; // 缺陷 100 → 48%
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int ring = Math.min(Math.min(x, SIZE - 1 - x), Math.min(y, SIZE - 1 - y));
                if (ring < 3) {
                    double p = switch (ring) {
                        case 0 -> base;
                        case 1 -> base * 0.6;
                        default -> base * 0.3;
                    };
                    if (random.nextDouble() < p) {
                        filled[idx(x, y)] = false;
                    }
                }
            }
        }
        removeEnclosedHoles();
        initialFilled = countFilled();
    }

    /**
     * 修复封闭孔洞：任何缺失格若四邻均为实格则视为「打洞」型缺失，恢复为实格。
     * 迭代直至稳定，保证缺失区域总是与边缘连通。
     */
    private void removeEnclosedHoles() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    int i = idx(x, y);
                    if (!filled[i] && neighborsAllFilled(x, y)) {
                        filled[i] = true;
                        changed = true;
                    }
                }
            }
        }
    }

    private boolean neighborsAllFilled(int x, int y) {
        return isFilled(x - 1, y) && isFilled(x + 1, y) && isFilled(x, y - 1) && isFilled(x, y + 1);
    }

    // ==================== 查询 ====================

    public boolean isFilled(int x, int y) {
        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) return false;
        return filled[idx(x, y)];
    }

    private static int idx(int x, int y) {
        return y * SIZE + x;
    }

    public int countFilled() {
        int n = 0;
        for (boolean f : filled) {
            if (f) n++;
        }
        return n;
    }

    public int getInitialFilled() {
        return initialFilled;
    }

    public int getStrikeCount() {
        return strikeCount;
    }

    public int getRemovedCount() {
        return Math.max(0, initialFilled - countFilled());
    }

    /** 目标格是否为实格（可作为打击点）。 */
    public boolean isValidTarget(int x, int y) {
        return isFilled(x, y);
    }

    // ==================== 打击 ====================

    /**
     * 一次打击：以 (cx, cy) 为中心、5×5 范围内按概率移除方格。
     *
     * @param power     蓄力力量 0~1
     * @param sharpness 工具锋利度 0~100
     * @return 实际移除的方格数
     */
    public int strike(int cx, int cy, double power, int sharpness, RandomSource random) {
        strikeCount++;
        double sharpFactor = 0.6 + 0.4 * (sharpness / 100.0);
        int removed = 0;
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                int x = cx + dx, y = cy + dy;
                if (!isFilled(x, y)) continue;
                int dist = Math.max(Math.abs(dx), Math.abs(dy));
                double p = switch (dist) {
                    case 0 -> 1.0;
                    case 1 -> 0.5 + 0.5 * power;
                    default -> 0.25 * power;
                } * sharpFactor;
                if (random.nextDouble() < p) {
                    filled[idx(x, y)] = false;
                    removed++;
                }
            }
        }
        return removed;
    }

    /** 力学与断裂判定结果。 */
    public enum FractureResult { NONE, EDGE_CHIP, SHATTER }

    /**
     * 每次打击后按「力学与断裂」掷骰：
     * 崩边 = 随机一条外边缘额外剥落一串方格；炸裂 = 整块石头报废。
     */
    public FractureResult rollFracture(int mechanics, RandomSource random) {
        double risk = 1.0 - mechanics / 100.0;
        if (random.nextDouble() < risk * 0.08) {
            return FractureResult.SHATTER;
        }
        if (random.nextDouble() < risk * 0.25) {
            chipRandomEdge(random);
            return FractureResult.EDGE_CHIP;
        }
        return FractureResult.NONE;
    }

    /** 崩边：在随机一条边上，从随机位置起剥落 4~8 个连续外层实格。 */
    private void chipRandomEdge(RandomSource random) {
        int edge = random.nextInt(4);
        int start = random.nextInt(SIZE);
        int len = 4 + random.nextInt(5);
        for (int i = 0; i < len; i++) {
            int t = start + i;
            if (t >= SIZE) break;
            int x, y;
            switch (edge) {
                case 0 -> { x = t; y = 0; }
                case 1 -> { x = t; y = SIZE - 1; }
                case 2 -> { x = 0; y = t; }
                default -> { x = SIZE - 1; y = t; }
            }
            if (isFilled(x, y)) {
                filled[idx(x, y)] = false;
            }
        }
    }

    // ==================== 成品属性 ====================

    /** 打制度：缺失方格占初始实格比例（0~100）。 */
    public int computeKnappedness() {
        if (initialFilled <= 0) return 0;
        return clamp((int) (getRemovedCount() * 100.0 / initialFilled));
    }

    /**
     * 尖锐度：检测左、右、上三面最边界方格轮廓的「尖锐峰值」。
     * 轮廓上某格比两侧邻格更突出（凸尖）且落差越大，尖锐度越高。
     */
    public int computePointedness() {
        int sum = profilePeaks(topProfile(), true)   // 顶面：向上凸出
                + profilePeaks(leftProfile(), true)  // 左面：向左凸出
                + profilePeaks(rightProfile(), false); // 右面：向右凸出
        return clamp((int) (sum * 100.0 / 45.0)); // 经验归一化：约 45 点落差总和 ≈ 满值
    }

    /** 每列最高实格的 y（无实格返回 SIZE）。 */
    private int[] topProfile() {
        int[] profile = new int[SIZE];
        java.util.Arrays.fill(profile, SIZE);
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (isFilled(x, y)) {
                    profile[x] = y;
                    break;
                }
            }
        }
        return profile;
    }

    /** 每行最左实格的 x（无实格返回 SIZE）。 */
    private int[] leftProfile() {
        int[] profile = new int[SIZE];
        java.util.Arrays.fill(profile, SIZE);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (isFilled(x, y)) {
                    profile[y] = x;
                    break;
                }
            }
        }
        return profile;
    }

    /** 每行最右实格的 x（无实格返回 -1）。 */
    private int[] rightProfile() {
        int[] profile = new int[SIZE];
        java.util.Arrays.fill(profile, -1);
        for (int y = 0; y < SIZE; y++) {
            for (int x = SIZE - 1; x >= 0; x--) {
                if (isFilled(x, y)) {
                    profile[y] = x;
                    break;
                }
            }
        }
        return profile;
    }

    /**
     * 峰值检测：profile[a] 比 profile[a±1] 更突出（向 outDir 方向）时记为凸尖，
     * 落差取两侧较小值。空列（边界值）不计。
     */
    private int profilePeaks(int[] profile, boolean outLower) {
        int sum = 0;
        for (int a = 1; a < SIZE - 1; a++) {
            int v = profile[a], l = profile[a - 1], r = profile[a + 1];
            if (v < 0 || v >= SIZE) continue; // 空列不计
            boolean peak = outLower ? (v < l && v < r) : (v > l && v > r);
            if (peak) {
                int drop = Math.min(Math.abs(l - v), Math.abs(r - v));
                sum += Math.max(1, drop);
            }
        }
        return sum;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }

    // ==================== 序列化 ====================

    /** 每格 1 bit，共 128 字节。 */
    public byte[] toBytes() {
        byte[] bytes = new byte[CELLS / 8];
        for (int i = 0; i < CELLS; i++) {
            if (filled[i]) {
                bytes[i >> 3] |= (byte) (1 << (i & 7));
            }
        }
        return bytes;
    }

    public void fromBytes(byte[] bytes) {
        for (int i = 0; i < CELLS; i++) {
            filled[i] = i < bytes.length * 8 && (bytes[i >> 3] & (1 << (i & 7))) != 0;
        }
    }

    /** 反序列化后恢复统计量（与方格内容一起存档）。 */
    public void restoreStats(int initialFilled, int strikeCount) {
        this.initialFilled = Math.max(1, initialFilled);
        this.strikeCount = strikeCount;
    }
}
