

package com.xiaoyv.dx.rop.code;

import com.xiaoyv.dx.rop.cst.Constant;
import com.xiaoyv.dx.rop.cst.CstInteger;
import com.xiaoyv.dx.rop.type.StdTypeList;
import com.xiaoyv.dx.rop.type.Type;
import com.xiaoyv.dx.rop.type.TypeBearer;
import com.xiaoyv.dx.rop.type.TypeList;

/**
 * Plain instruction, which has no embedded data and which cannot possibly
 * throw an exception.
 */
public final class PlainInsn
        extends Insn {
    /**
     * Constructs an instance.
     *
     * @param opcode   {@code non-null;} the opcode
     * @param position {@code non-null;} source position
     * @param result   {@code null-ok;} spec for the result, if any
     * @param sources  {@code non-null;} specs for all the sources
     */
    public PlainInsn(Rop opcode, SourcePosition position,
                     RegisterSpec result, RegisterSpecList sources) {
        super(opcode, position, result, sources);

        switch (opcode.getBranchingness()) {
            case Rop.BRANCH_SWITCH:
            case Rop.BRANCH_THROW: {
                throw new IllegalArgumentException("bogus branchingness");
            }
        }

        if (result != null && opcode.getBranchingness() != Rop.BRANCH_NONE) {
            // move-result-pseudo is required here
            throw new IllegalArgumentException
                    ("can't mix branchingness with result");
        }
    }

    /**
     * Constructs a single-source instance.
     *
     * @param opcode   {@code non-null;} the opcode
     * @param position {@code non-null;} source position
     * @param result   {@code null-ok;} spec for the result, if any
     * @param source   {@code non-null;} spec for the source
     */
    public PlainInsn(Rop opcode, SourcePosition position, RegisterSpec result,
                     RegisterSpec source) {
        this(opcode, position, result, RegisterSpecList.make(source));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeList getCatches() {
        return StdTypeList.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(Visitor visitor) {
        visitor.visitPlainInsn(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Insn withAddedCatch(Type type) {
        throw new UnsupportedOperationException("unsupported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Insn withRegisterOffset(int delta) {
        return new PlainInsn(getOpcode(), getPosition(),
                getResult().withOffset(delta),
                getSources().withOffset(delta));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Insn withSourceLiteral() {
        RegisterSpecList sources = getSources();
        int szSources = sources.size();

        if (szSources == 0) {
            return this;
        }

        TypeBearer lastType = sources.get(szSources - 1).getTypeBearer();

        if (!lastType.isConstant()) {
            // Check for reverse subtraction, where first source is constant
            TypeBearer firstType = sources.get(0).getTypeBearer();
            if (szSources == 2 && firstType.isConstant()) {
                Constant cst = (Constant) firstType;
                RegisterSpecList newSources = sources.withoutFirst();
                Rop newRop = Rops.ropFor(getOpcode().getOpcode(), getResult(),
                        newSources, cst);
                return new PlainCstInsn(newRop, getPosition(), getResult(),
                        newSources, cst);
            }
            return this;
        } else {

            Constant cst = (Constant) lastType;

            RegisterSpecList newSources = sources.withoutLast();

            Rop newRop;
            try {
                // Check for constant subtraction and flip it to be addition
                int opcode = getOpcode().getOpcode();
                if (opcode == RegOps.SUB && cst instanceof CstInteger) {
                    opcode = RegOps.ADD;
                    cst = CstInteger.make(-((CstInteger) cst).getValue());
                }
                newRop = Rops.ropFor(opcode, getResult(), newSources, cst);
            } catch (IllegalArgumentException ex) {
                // There's no rop for this case
                return this;
            }

            return new PlainCstInsn(newRop, getPosition(),
                    getResult(), newSources, cst);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Insn withNewRegisters(RegisterSpec result,
                                 RegisterSpecList sources) {

        return new PlainInsn(getOpcode(), getPosition(),
                result,
                sources);

    }
}
