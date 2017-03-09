package confucian.combinedTest;

import java.util.Arrays;

/**
 * 分子类型
 */
class Molecule {
    /**
     * 原子
     */
    private int[] atoms;

    /**
     * 初始化没有原子索引集合的分子
     */
    Molecule() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Molecule that = (Molecule) o;

        return atoms != null ?
                Arrays.equals(atoms, that.getAtoms()) :
                that.getAtoms() == null;
    }

    @Override
    public int hashCode() {
        return atoms != null ?
                Arrays.hashCode(atoms) :
                0;
    }

    @Override
    public String toString() {
        return Arrays.toString(getAtoms());
    }

    /**
     * 获取原子数组
     *
     * @return the int [ ]
     */
    int[] getAtoms() {
        return atoms;
    }

    /**
     * 设置原子
     *
     * @param atoms the atoms
     */
    void setAtoms(int[] atoms) {
        this.atoms = atoms;
    }
}
