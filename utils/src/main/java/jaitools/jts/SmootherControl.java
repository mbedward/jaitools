package jaitools.jts;

/**
 * Defines methods to control the smoothing process.
 * {@code LineSmoother} has a default implementation
 * that specifies a constant number of vertices in smoothed
 * segments and no lower bound on the distance between
 * input vertices for smoothing.
 * <p>
 * To customize smoothing, pass your own implementation
 * to {@link LineSmoother#setControl(jaitools.jts.LineSmoother.Control) }.
 */
public interface SmootherControl {

    /**
     * Gets the minimum distance between input vertices
     * for the segment to be smoothed. Segments smaller
     * than this will be copied to the output unchanged.
     *
     * @return minimum segment length for smoothing
     */
    double getMinLength();

    /**
     * Given an input segment length, returns the number
     * of vertices to use for the smoothed segment. This
     * number includes the segment end-points.
     *
     * @param length input segment length
     *
     * @return number of vertices in the smoothed segment
     *         including the end-points
     */
    int getNumVertices(double length);
}
