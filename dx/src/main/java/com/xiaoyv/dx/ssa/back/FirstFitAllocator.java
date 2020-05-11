

package com.xiaoyv.dx.ssa.back;

import com.xiaoyv.dx.rop.code.CstInsn;
import com.xiaoyv.dx.rop.cst.CstInteger;
import com.xiaoyv.dx.ssa.BasicRegisterMapper;
import com.xiaoyv.dx.ssa.NormalSsaInsn;
import com.xiaoyv.dx.ssa.RegisterMapper;
import com.xiaoyv.dx.ssa.SsaMethod;
import com.xiaoyv.dx.util.BitIntSet;
import com.xiaoyv.dx.util.IntSet;

import java.util.BitSet;

/**
 * Allocates registers via a naive n^2 register allocator.
 * This allocator does not try to co-locate local variables or deal
 * intelligently with different size register uses.
 */
public class FirstFitAllocator extends RegisterAllocator {
    /**
     * If true, allocator places parameters at the top of the frame
     * in calling-convention order.
     */
    private static final boolean PRESLOT_PARAMS = true;

    /**
     * indexed by old reg; the set of old regs we've mapped
     */
    private final BitSet mapped;

    /**
     * {@inheritDoc}
     */
    public FirstFitAllocator(
            final SsaMethod ssaMeth, final InterferenceGraph interference) {
        super(ssaMeth, interference);

        mapped = new BitSet(ssaMeth.getRegCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean wantsParamsMovedHigh() {
        return PRESLOT_PARAMS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegisterMapper allocateRegisters() {
        int oldRegCount = ssaMeth.getRegCount();

        BasicRegisterMapper mapper
                = new BasicRegisterMapper(oldRegCount);

        int nextNewRegister = 0;

        if (PRESLOT_PARAMS) {
            /*
             * Reserve space for the params at the bottom of the register
             * space. Later, we'll flip the params to the end of the register
             * space.
             */

            nextNewRegister = ssaMeth.getParamWidth();
        }

        for (int i = 0; i < oldRegCount; i++) {
            if (mapped.get(i)) {
                // we already got this one
                continue;
            }

            int maxCategory = getCategoryForSsaReg(i);
            IntSet current = new BitIntSet(oldRegCount);

            interference.mergeInterferenceSet(i, current);

            boolean isPreslotted = false;
            int newReg = 0;

            if (PRESLOT_PARAMS && isDefinitionMoveParam(i)) {
                // Any move-param definition must be a NormalSsaInsn
                NormalSsaInsn defInsn = (NormalSsaInsn)
                        ssaMeth.getDefinitionForRegister(i);

                newReg = paramNumberFromMoveParam(defInsn);

                mapper.addMapping(i, newReg, maxCategory);
                isPreslotted = true;
            } else {
                mapper.addMapping(i, nextNewRegister, maxCategory);
                newReg = nextNewRegister;
            }

            for (int j = i + 1; j < oldRegCount; j++) {
                if (mapped.get(j) || isDefinitionMoveParam(j)) {
                    continue;
                }

                /*
                 * If reg j doesn't interfere with the current mapping.
                 * Also, if this is a pre-slotted method parameter, we
                 * can't use more than the original param width.
                 */
                if (!current.has(j)
                        && !(isPreslotted
                        && (maxCategory < getCategoryForSsaReg(j)))) {

                    interference.mergeInterferenceSet(j, current);

                    maxCategory = Math.max(maxCategory,
                            getCategoryForSsaReg(j));

                    mapper.addMapping(j, newReg, maxCategory);
                    mapped.set(j);
                }
            }

            mapped.set(i);
            if (!isPreslotted) {
                nextNewRegister += maxCategory;
            }
        }

        return mapper;
    }

    /**
     * Returns the parameter number that this move-param insn refers to
     *
     * @param ndefInsn a move-param insn (otherwise, exceptions will be thrown)
     * @return parameter number (offset in the total parameter width)
     */
    private int paramNumberFromMoveParam(NormalSsaInsn ndefInsn) {
        CstInsn origInsn = (CstInsn) ndefInsn.getOriginalRopInsn();

        return ((CstInteger) origInsn.getConstant()).getValue();
    }
}
