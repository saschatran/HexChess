package ch.unibas.dmi.dbis.cs108.project;

/**
 * general purpose Graphics Engine
 */


public class GFX {

    /**
     * Variables used for tiles:
     * oam: each entry represents one tile; it is of the following format:
     *      -BII IIII, where - denotes unused bits, B denotes the priority bit and I denotes the palette index
     * pos: position of each tile; normalised (x,y in [0.0,1.0])
     * tilemap: array of all tiles (tilemap[INDEX][Y][X] contains the colour of tile number INDEX at coordinates (X,Y))
     * palRGBA: array of all colour palettes (palRGBA[INDEX][NO][CHN] contains the (NO)th colour in palette number INDEX for colour channel CHN)
     *
     */
    static byte[] oam;
    static double[][] pos;
    static byte[][][] tilemap;
    static double[][][] palRGBA;


    /**
     * Variables for the background:
     * bg0: background (at the moment, only 1 is needed, hence no bg1,...) (in 24 bit RGB)
     * bgcol: fallback colour, if nothing is on a pixel
     */
    static double[][][] bg0;
    static int bgcol = 0x000000; //0x000000 is black, 0xffffff is white...

    /**
     * Variables used for the background transformation:
     * aParams: Matrix for affine transformation
     * aOrigin: Origin of affine transformation (not in use yet)
     * displayList: Set of instructions to alter the affine matrix on a row-by-row basis
     * displayListIdx: Index of the next display List entry (only used by processDL())
     * displayListWait: The number of rows the affine matrix stays constant
     * normalised: treat first entry of displayList as column count instead of normalised column count
     * fullDL: Use Dl with each entry containing 6 params, otherwise treat it as a collection of address:data-pairs
     * resetDLI: reset displayListIdx on each new frame
     */
    static double[][] aParams;
    static double[] aOrigin;
    static double[] displayList;
    static int displayListIdx = 0;
    static int displayListWait;
    static boolean normalised = true;
    static boolean fullDL = false;
    static boolean resetDLI = true;
    /**
     * framebuffers, each in the format: y,x,RGB,
     * where RGB is an array containing entries for red, green and blue (each usually ranging from 0.0 to 255.0)
     * fb is used as an alias to the current framebuffer in use.
     */
    static double[][][] fbufEven;
    static double[][][] fbufOdd;
    static double[][][] fb;
    static boolean even = false;

    /**
     * Normalise barycentric coord.
     * @param vecH barycentric vector (in R3)
     * @return barycentric vector (normalised) (in R2 x {1})
     */
    public static double[] normaliseB(double[] vecH) {
        vecH[0] /= vecH[2];
        vecH[1] /= vecH[2];
        vecH[2] = 1;
        return vecH;
    }

    /**
     * Perform affine transformation on barycentric vector
     * @param vecH initial vector
     * @return (vector)*(affine Matrix)
     */
    public static double[] affineTB(int[] vecH) {
        double x = (vecH[0] - aOrigin[0]) * aParams[0][0] + (vecH[1] - aOrigin[1]) * aParams[0][1] + vecH[2] * aParams[0][2] + aOrigin[0];
        double y = (vecH[0] - aOrigin[0]) * aParams[1][0] + (vecH[1] - aOrigin[1]) * aParams[1][1] + vecH[2] * aParams[1][2] + aOrigin[1];
        return new double[]{x,y,1};
    }

    /**
     * Perform affine transformation on barycentric vector
     * @param vecH initial vector
     * @return (vector)*(affine Matrix)
     */
    public static double[] affineTB(double[] vecH) {
        double x = vecH[0] * aParams[0][0] + vecH[1] * aParams[0][1] + vecH[2] * aParams[0][2];
        double y = vecH[0] * aParams[1][0] + vecH[1] * aParams[1][1] + vecH[2] * aParams[1][2];
        return new double[]{x,y,1};
    }

    /**
     * Perform affine transformation on background layer
     * @param bg background layer to perform transformation on
     * @param winX width of frame
     * @param winY height of frame
     * @return transformed background
     */
    public static double[][][] transformbg(double[][][] bg, int winX, int winY) {
        double[][][] out = new double[winX][winY][4];
        double[][] tmp = new double[2][4];
        double[] posA = {0,0,1};
        int xf;
        int xc;
        int yf;
        int yc;
        int[] pos = {0,0,1};
        for(pos[0] = 0; pos[0] < winX; pos[0]++){
            for(pos[1] = 0; pos[1] < winY; pos[1]++){
                posA= affineTB(pos);
                xf=Math.max((int) Math.floor(posA[0]),0);
                xc=Math.min((int) Math.ceil(posA[0]),winX);
                yf=Math.max((int) Math.floor(posA[1]),0);
                yc=Math.min((int) Math.ceil(posA[1]),winY);
                for (int i = 0; i < 4; i++) {
                    //bilinear for col interpolation
                    tmp[0][0] = bg0[xf][yf][i] * (xc - posA[0]) + bg0[xc][yf][i] * (posA[0] - xf);
                    tmp[1][0] = bg0[xf][yc][i] * (xc - posA[0]) + bg0[xc][yc][i] * (posA[0] - xf);

                    out[pos[0]][pos[1]][i] = tmp[0][i] * (yc - posA[1]) + tmp[1][i] * (posA[1] - yf);
                }
            }
        }
        return out;
    }

    /**
     * Perform affine transformation one column of background layer (MUCH faster, if big displayList is used)
     * @param bg background layer to perform transformation on
     * @param winX width of frame
     * @param y current y-ordinate
     * @return transformed line
     */
    public static double[][] transformbgLine(double[][][] bg, int winX, int y) {
        double[] posA={0,0,1};
        int xf;
        int xc;
        int yf;
        int yc;
        double[][] tmp =new double[2][4];
        double[][] out=new double[winX][4];
        for (int x = 0; x < winX; x++) {
            posA= affineTB(new int[]{ x, y, 1});
            xf=(int) Math.floor(posA[0]);
            xc=(int) Math.ceil(posA[0]);
            yf=(int) Math.floor(posA[1]);
            yc=(int) Math.ceil(posA[1]);
            for (int i = 0; i < 4; i++) {
                tmp[0][i] = bg0[xf][yf][i] * (xc - posA[0]) + bg0[xc][yf][i] * (posA[0] - xf);
                tmp[1][i] = bg0[xf][yc][i] * (xc - posA[0]) + bg0[xc][yc][i] * (posA[0] - xf);

                out[x][i] = tmp[0][i] * (yc - posA[1]) + tmp[1][i] * (posA[1] - yf);
            }
        }
        return out;
    }

    /**
     * Main graphics function, to be called on each frame to render.
     * Behind everything is the background colour (bgcol) in 24 bit RGB (8 bit for Red, 8 for Blue, 8 for Green)
     * followed by the background (bg0) in 32 bit RGBA (8 bit for Alpha, the opacity)
     * then tiles (chess pieces etc.) in 32 bit RGBA with lower priority
     * and above everything, tiles with high priority (e.g. moving chess pieces, capturing chess pieces etc.)
     *
     * This is likely the only function to be used outside of this file.
     * @param winX width of frame
     * @param winY height of frame
     * @param redraw regenerate the background (not needed, if frame size hasn't changed)
     * @return current frame with each colour consisting of 3 channels (red, green, blue) in range [0,255]
     */

    public static double[][][] pipeline(int winX, int winY, boolean redraw) {
        if (even){
            fb=fbufEven;
        }
        else{
            fb=fbufOdd;
        }
        // reset displayList
        displayListWait = 0;
        if (resetDLI) {
            displayListIdx = 0;
        }
        if(redraw){
            //bg0 = generate chess board (width,height)
        }
        for (int y = 0; y < winY; y++) {
            for (int x = 0; x < winX; x++) {
                fb[y][x][0]=(bgcol & 0xff0000) >> 16;
                fb[y][x][1]=(bgcol & 0xff00) >> 8;
                fb[y][x][2]=(bgcol & 0xff);
            }
        }
        //rescale Background, if not correct size and in use (bg0.length!=0)
        if (bg0.length != 0 && (bg0[0].length != winX || bg0.length != winY)) {
            double factorX = winX;
            double factorY = winY;
            factorX /= bg0[0].length;
            factorY /= bg0.length;
            rescale(bg0, factorX, factorY, true);
        }
        //Assemble Frame
        double[] tmpRGBA = new double[] {0, 0, 0, 0};
        int dx;
        int dy;
        int tx;
        int ty;
        for (int y = 0; y < winY; y++) {
            processDL(winY);
            if(bg0.length != 0){
                bg0 = transformbg(bg0, winX, winY);
            }
            for (int x = 0; x < winX; x++) {
                if(bg0.length != 0){
                    fb[y][x] = getColour(fb[y][x], bg0[y][x]);
                }
                //apply tiles
                for (int priority = 0; priority < 2; priority++) {
                    for (int idx = 0; idx < tilemap.length; idx++) {
                        if((oam[idx] & 0x40) >> 6!= priority){
                            continue;
                        }
                        dy = (int) Math.round(pos[idx][1]*winY);
                        ty = y - dy;
                        if((ty < 0) || (ty >= tilemap[idx].length)) { //tile not in this row
                            continue;
                        }
                        dx = (int) Math.round(pos[idx][0]*winX);
                        tx = x - dx;
                        if((tx < 0) || (tx >= tilemap[idx][0].length)) { //tile not in this column
                            continue;
                        }
                        tmpRGBA = palRGBA[oam[idx] & 0x3f][tilemap[idx][ty][tx]];
                        fb[y][x] = getColour(fb[y][x], tmpRGBA);
                    }
                }
            }
        }
        short tmp;
        for (int y = 0; y < fb.length; y++) {
            for (int x = 0; x < fb[0].length; x++) {
                for (int channel = 0; channel < 3; channel++) {
                    tmp = (short) fb[y][x][channel];
                    tmp = (short) Math.max(0, Math.min(tmp, 255)); //Limit Colours to [0,255] (8-bit) for each colour channel
                    fb[y][x][channel] = tmp;
                }
            }
        }
        return fb;
    }

    /**
     * Allows alteration of parameters e.g. affine matrix on every row for advanced effects (e.g. perspective...)
     * @param winY frame width
     */
    public static void processDL( int winY) {
        if (displayListWait != 0){
            if (displayListWait != -1){ //Display List finished for current frame
                displayListWait--;
            }
            return;
        }
        if (displayList[displayListIdx] < -1.0){ //Display List ended
            displayListWait = -1;
            return;
        }
        if (normalised){ // first Line=0, last =1
            displayListWait = (int) Math.round(displayList[displayListIdx]*winY);
        }
        else{ //first Line=0, last = winX
            displayListWait = (int) Math.floor(displayList[displayListIdx]);
        }
        if (fullDL){
            for (int i = 0; i < 6; i++) {
                aParams[i/3][i%3] = displayList[displayListIdx + 1 + i];
                displayListIdx += 7;
            }
        }
        else{ //entries in Range[0,5] are for affineMX, in [6,7] for aOrigin, in [8,71] for Palette (next: colNo, then 4x value)
            displayListIdx += 1;
            while (displayList.length > displayListIdx + 1){
                if (displayList[displayListIdx] < -0.5){
                    displayListIdx++;
                    return;
                }
                if(displayList[displayListIdx] < 6){
                    //affineMX
                    int idx = (int) Math.round(displayList[displayListIdx]);
                    aParams[idx%3][idx/3] = displayList[displayListIdx + 1];
                    displayListIdx += 2;
                }
                else{
                    if(displayList[displayListIdx] < 8){
                        //affineOrigin
                        int idx = (int) Math.round(displayList[displayListIdx]) - 6;
                        aOrigin[idx] = displayList[displayListIdx + 1];
                        displayListIdx += 2;
                    }
                    else{
                        if(displayList[displayListIdx] < 72){
                            //change colour in palette
                            int palno = (int) Math.round(displayList[displayListIdx]) - 8;
                            int cno = (int) Math.round(displayList[displayListIdx + 1]);
                            if (cno>255){
                                System.out.println("DL colour no exceeds no of colours.");
                                //deactivate DL
                                displayListWait = -1;
                                return;
                            }else{
                                palRGBA[palno][cno][0]=displayList[displayListIdx + 2];
                                palRGBA[palno][cno][1]=displayList[displayListIdx + 3];
                                palRGBA[palno][cno][2]=displayList[displayListIdx + 4];
                                palRGBA[palno][cno][3]=displayList[displayListIdx + 5];
                                displayListIdx += 6;
                            }
                        }
                        else{
                            System.out.println("DisplayList cmd exceeds current possiblilties.");
                            //deactivate DL
                            displayListWait = -1;
                            return;
                        }
                    }
                }
            }
        }
        return;
    }

    /**
     * Achieves colour mixing for transparency effects
     * @param col1 colour currently in the framebuffer at pixel x,y
     * @param rgba colour to be added at pixel x,y
     * @return mixed colour
     */
    public static double[] getColour(double[] col1, double[] rgba) {
        double factor = (rgba[3])/255.0;
        double r = (factor-1)*(col1[0]) + factor * (rgba[0]);
        double g = (factor-1)*(col1[1]) + factor * (rgba[1]);
        double b = (factor-1)*(col1[2]) + factor * (rgba[2]);
        return new double [] {r, g, b};
    }

    /**
     * rescales the image
     * @param tile the data to be up- or downscaled
     * @param factorX the factor to be scaled with (width)
     * @param factorY the factor to be scaled with (height)
     * @param fast the algorithm to use:
     *             if true, use nearest neighbor (lines stay precise and high speed, but lower quality)
     *             if false, use kernel (lines fade and lower speed, but higher quality)
     * @return rescaled image
     */
    public static double[][][] rescale(double[][][] tile, double factorX, double factorY, boolean fast) {
        System.out.println("Not yet ready.");
        return null;
    }

    /**
     * rescales the tile
     * @param tile the data to be up- or downscaled
     * @param factorX the factor to be scaled with (width)
     * @param factorY the factor to be scaled with (height)
     * @param fast the algorithm to use:
     *             if true, use nearest neighbor (high speed and lines stay precise, but lower quality)
     *             if false, use kernel (lower speed and lines fade, but higher quality)
     * @param palIdx the palette to use
     *               (upscaling with non-integers and downscaling require interpolation and therefore colours to interpolate with.
     * @return rescaled tile
     */
    public static double[][][] rescale(double[][][] tile, double factorX, double factorY, boolean fast, int palIdx) {
        System.out.println("Not yet ready.");
        return null;
    }

}
