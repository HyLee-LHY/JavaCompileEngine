

package com.xiaoyv.dx.dex.code;

import com.xiaoyv.dx.rop.code.BasicBlock;
import com.xiaoyv.dx.rop.code.BasicBlockList;
import com.xiaoyv.dx.rop.code.Insn;
import com.xiaoyv.dx.rop.code.RopMethod;
import com.xiaoyv.dx.rop.code.SourcePosition;

/**
 * Container for the set of {@link CodeAddress} instances associated with
 * the blocks of a particular method. Each block has a corresponding
 * start address, end address, and last instruction address.
 */
public final class BlockAddresses {
    /**
     * {@code non-null;} array containing addresses for the start of each basic
     * block (indexed by basic block label)
     */
    private final CodeAddress[] starts;

    /**
     * {@code non-null;} array containing addresses for the final instruction
     * of each basic block (indexed by basic block label)
     */
    private final CodeAddress[] lasts;

    /**
     * {@code non-null;} array containing addresses for the end (just past the
     * final instruction) of each basic block (indexed by basic block
     * label)
     */
    private final CodeAddress[] ends;

    /**
     * Constructs an instance.
     *
     * @param method {@code non-null;} the method to have block addresses for
     */
    public BlockAddresses(RopMethod method) {
        BasicBlockList blocks = method.getBlocks();
        int maxLabel = blocks.getMaxLabel();

        this.starts = new CodeAddress[maxLabel];
        this.lasts = new CodeAddress[maxLabel];
        this.ends = new CodeAddress[maxLabel];

        setupArrays(method);
    }

    /**
     * Gets the instance for the start of the given block.
     *
     * @param block {@code non-null;} the block in question
     * @return {@code non-null;} the appropriate instance
     */
    public CodeAddress getStart(BasicBlock block) {
        return starts[block.getLabel()];
    }

    /**
     * Gets the instance for the start of the block with the given label.
     *
     * @param label {@code non-null;} the label of the block in question
     * @return {@code non-null;} the appropriate instance
     */
    public CodeAddress getStart(int label) {
        return starts[label];
    }

    /**
     * Gets the instance for the final instruction of the given block.
     *
     * @param block {@code non-null;} the block in question
     * @return {@code non-null;} the appropriate instance
     */
    public CodeAddress getLast(BasicBlock block) {
        return lasts[block.getLabel()];
    }

    /**
     * Gets the instance for the final instruction of the block with
     * the given label.
     *
     * @param label {@code non-null;} the label of the block in question
     * @return {@code non-null;} the appropriate instance
     */
    public CodeAddress getLast(int label) {
        return lasts[label];
    }

    /**
     * Gets the instance for the end (address after the final instruction)
     * of the given block.
     *
     * @param block {@code non-null;} the block in question
     * @return {@code non-null;} the appropriate instance
     */
    public CodeAddress getEnd(BasicBlock block) {
        return ends[block.getLabel()];
    }

    /**
     * Gets the instance for the end (address after the final instruction)
     * of the block with the given label.
     *
     * @param label {@code non-null;} the label of the block in question
     * @return {@code non-null;} the appropriate instance
     */
    public CodeAddress getEnd(int label) {
        return ends[label];
    }

    /**
     * Sets up the address arrays.
     */
    private void setupArrays(RopMethod method) {
        BasicBlockList blocks = method.getBlocks();
        int sz = blocks.size();

        for (int i = 0; i < sz; i++) {
            BasicBlock one = blocks.get(i);
            int label = one.getLabel();
            Insn insn = one.getInsns().get(0);

            starts[label] = new CodeAddress(insn.getPosition());

            SourcePosition pos = one.getLastInsn().getPosition();

            lasts[label] = new CodeAddress(pos);
            ends[label] = new CodeAddress(pos);
        }
    }
}
