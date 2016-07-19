package org.cora.extract_pdf_excel.data.lane;

import org.cora.extract_pdf_excel.data.block.Block;

import java.util.*;

/**
 * Created by eadgyo on 12/07/16.
 */
public class Lane
{
    private Rect                   rectangle;
    private TreeMap<Double, Block> blocks;

    public Lane()
    {
        rectangle = new Rect();
        blocks = new TreeMap<Double, Block>();
    }

    public void setRectangle(Rect rectangle)
    {
        this.rectangle.setRect(rectangle);
    }

    public double getPos(int i) { return this.rectangle.getPos(i); }

    public double getLength(int i) { return this.rectangle.getLength(i); }

    public void setPos(int i, double value)
    {
        this.rectangle.setPos(i, value);
    }

    public void setLength(int i, double value)
    {
        this.rectangle.setLength(i, value);
    }

    public TreeMap<Double, Block> getBlocks()
    {
        return blocks;
    }

    public void setBlocks(TreeMap<Double, Block> blocks)
    {
        this.blocks = blocks;
    }

    public Set<Block> copyBlocks() { return new HashSet<>(blocks.values()); }

    public void clearBlocks() { blocks.clear(); }

    /**
     * Add block to the lane and fit bound lane to added block
     *
     * @param axis         lane axis
     * @param oppositeAxis opposite axis of the lane
     * @param block        added block
     */
    public void addBlockAndFitLane(int axis, int oppositeAxis, Block block)
    {
        addBlock(axis, block);
        fitLane(axis, oppositeAxis, block);
    }

    /**
     * Add block to lane without updating lane bound
     *
     * @param axis lane axis
     * @param block added block
     */
    public void addBlock(int axis, Block block)
    {
        blocks.put(block.getPos(axis), block);
    }

    /**
     * Fit lane to added rectangle
     *
     * @param axis         lane axis
     * @param oppositeAxis lane opposite axis
     * @param block        added block
     */
    public void fitLane(int axis, int oppositeAxis, Block block)
    {
        // On main axis
        fitLaneOneAxis(axis, block);

        // On opposite axis
        fitLaneOneAxis(oppositeAxis, block);
    }

    /**
     * Fit one axis lane to added block
     *
     * @param axis  lane axis
     * @param block added blocks
     */
    private void fitLaneOneAxis(int axis, Block block)
    {
        // If block lower coordinate is before lane lower coordinate
        if (block.getPos(axis) < rectangle.getPos(axis))
        {
            // Change lane lower coordinate to block lower coordinate
            rectangle.addLength(axis, rectangle.getPos(axis) - block.getPos(axis));
            rectangle.setPos(axis, block.getPos(axis));
        }

        // If block higher coordinate is after lane higher coordinate
        if (block.getPos(axis) + block.getLength(axis) > rectangle.getPos(axis) + rectangle.getLength(axis))
        {
            // Change lane higher coordinate to lane higher coordinate
            rectangle.setLength(axis, block.getPos(axis) + block.getLength(axis) - rectangle.getPos(axis));
        }
    }

    /**
     * Get block lower than key
     *
     * @param key to be checked
     * @return lower block and his value in map
     */
    public Map.Entry<Double, Block> getLowerBlockEntry(double key)
    {
        return blocks.lowerEntry(key);
    }

    /**
     * Get block lower than key
     *
     * @param key to be checked
     * @return lower block
     */
    public Block getLowerBlock(double key)
    {
        Map.Entry<Double, Block> lowerBlockEntry = getLowerBlockEntry(key);

        return (lowerBlockEntry != null) ? lowerBlockEntry.getValue() : null;
    }

    /**
     * Get block higher than key
     *
     * @param key to be checked
     * @return higher block and his value in map
     */
    public Map.Entry<Double, Block> getHigherBlockEntry(double key)
    {
        return blocks.higherEntry(key);
    }

    /**
     * Get block higher than key
     *
     * @param key to be checked
     * @return higher block
     */
    public Block getHigherBlock(double key)
    {
        Map.Entry<Double, Block> higherBlockEntry = getHigherBlockEntry(key);

        return (higherBlockEntry != null) ? higherBlockEntry.getValue() : null;
    }

    public Rect getBound()
    {
        return rectangle;
    }

    public Block remove(Double key)
    {
        return blocks.remove(key);
    }

    /**
     * Reset lane bound and fit lane to in lanes' block. If there are no blocks, set the length to 0
     *
     * @param axis         lane axis
     * @param oppositeAxis opposite axis of the lane
     */
    public void resetAndFitLaneToInBlocks(int axis, int oppositeAxis)
    {
        // If there are no blocks in lane
        if (blocks.size() == 0)
        {
            setLength(axis, 0);
            setLength(oppositeAxis, 0);
            return;
        }

        Collection<Block> values = blocks.values();

        // Start to reset lane size with first block
        Iterator<Block> iterator = values.iterator();
        Block first = iterator.next();
        this.rectangle.setRect(first.getBound());

        // Reinsert all blocks
        while (iterator.hasNext())
        {
            Block reinsertedBlock = iterator.next();
            fitLaneOneAxis(axis, reinsertedBlock);
            fitLaneOneAxis(oppositeAxis, reinsertedBlock);
        }
    }

    /**
     * Fit lane to an higher lane. Only end bound of lane is changed.
     *
     * @param oppositeAxis opposite lane axis
     * @param higherLane lane higher than actual lane
     */
    public void fitToHigherLane(int oppositeAxis, Lane higherLane)
    {
        double maxLength = higherLane.getPos(oppositeAxis) - getPos(oppositeAxis);

        setLength(oppositeAxis, Math.min(getLength(oppositeAxis), maxLength));
    }
}