//Kyle Kodani hw6
//with assistance from: piazza, http://docs.oracle.com/javase/7/docs/api/

import java.io.*;
import java.util.*;
import java.math.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;

// a marker for code that you need to implement
class ImplementMe extends RuntimeException {}

// an RGB triple
class RGB {
    public int R, G, B;

    RGB(int r, int g, int b) {
	R = r;
	G = g;
	B = b;
    }

    public String toString() { return "(" + R + "," + G + "," + B + ")"; }

}

// code for creating a Gaussian filter
class Gaussian {

    protected static double gaussian(int x, int mu, double sigma) {
	return Math.exp( -(Math.pow((x-mu)/sigma,2.0))/2.0 );
    }

    public static double[][] gaussianFilter(int radius, double sigma) {
	int length = 2 * radius + 1;
	double[] hkernel = new double[length];
	for(int i=0; i < length; i++)
	    hkernel[i] = gaussian(i, radius, sigma);
	double[][] kernel2d = new double[length][length];
	double kernelsum = 0.0;
	for(int i=0; i < length; i++) {
	    for(int j=0; j < length; j++) {
		double elem = hkernel[i] * hkernel[j];
		kernelsum += elem;
		kernel2d[i][j] = elem;
	    }
	}
	for(int i=0; i < length; i++) {
	    for(int j=0; j < length; j++)
		kernel2d[i][j] /= kernelsum;
	}
	return kernel2d;
    }
}

// an object representing a single PPM image
class PPMImage {
    protected int width, height, maxColorVal;
    protected RGB[] pixels;

    protected static final int SEQUENTIAL_THRESHOLD = 10000;

    PPMImage(int w, int h, int m, RGB[] p) {
	width = w;
	height = h;
	maxColorVal = m;
	pixels = p;
    }

	// parse a PPM file to produce a PPMImage
    public static PPMImage fromFile(String fname) throws FileNotFoundException, IOException {
	FileInputStream is = new FileInputStream(fname);
	BufferedReader br = new BufferedReader(new InputStreamReader(is));
	br.readLine(); // read the P6
	String[] dims = br.readLine().split(" "); // read width and height
	int width = Integer.parseInt(dims[0]);
	int height = Integer.parseInt(dims[1]);
	int max = Integer.parseInt(br.readLine()); // read max color value
	br.close();

	is = new FileInputStream(fname);
	    // skip the first three lines
	int newlines = 0;
	while (newlines < 3) {
	    int b = is.read();
	    if (b == 10)
		newlines++;
	}

	int MASK = 0xff;
	int numpixels = width * height;
	byte[] bytes = new byte[numpixels * 3];
        is.read(bytes);
	RGB[] pixels = new RGB[numpixels];
	for (int i = 0; i < numpixels; i++) {
	    int offset = i * 3;
	    pixels[i] = new RGB(bytes[offset] & MASK, bytes[offset+1] & MASK, bytes[offset+2] & MASK);
	}

	return new PPMImage(width, height, max, pixels);
    }

	// write a PPMImage object to a file
    public void toFile(String fname) throws IOException {
	FileOutputStream os = new FileOutputStream(fname);

	String header = "P6\n" + width + " " + height + "\n" + maxColorVal + "\n";
	os.write(header.getBytes());

	int numpixels = width * height;
	byte[] bytes = new byte[numpixels * 3];
	int i = 0;
	for (RGB rgb : pixels) {
	    bytes[i] = (byte) rgb.R;
	    bytes[i+1] = (byte) rgb.G;
	    bytes[i+2] = (byte) rgb.B;
	    i += 3;
	}
	os.write(bytes);
    }
   
    public PPMImage negate() {
	ForkJoinPool pool = new ForkJoinPool();
	RGB[] negpix=new RGB[pixels.length];
	NegTask t=new NegTask(0, pixels.length, pixels, negpix, width, height, maxColorVal);
	pool.invoke(t);
	return new PPMImage(width, height, maxColorVal, negpix);
    }

    public PPMImage mirrorImage() {
	ForkJoinPool pool = new ForkJoinPool();
        RGB[] mirrorpix=new RGB[pixels.length];
        MirrorTask t=new MirrorTask(0, height, pixels, mirrorpix,
					width, height, maxColorVal);
        pool.invoke(t);
        return new PPMImage(width, height, maxColorVal, mirrorpix);
    }

    //doesn't work or works very slowly
    public PPMImage gaussianBlur(int radius, double sigma) {
	double[][] g=Gaussian.gaussianFilter(radius, sigma);
	ForkJoinPool pool = new ForkJoinPool();
	RGB[] blurpix=new RGB[pixels.length];
        BlurTask t=new BlurTask(0, height, pixels, blurpix,
                                        width, height, maxColorVal, g);
        pool.invoke(t);
        return new PPMImage(width, height, maxColorVal, blurpix);
    }
}

//helper class for negate
class NegTask extends RecursiveAction{
	private int start;
	private int end;
	private RGB[] source;
	private RGB[] dest;	
	private int width;
	private int height;
	private int max;

	protected static final int SEQUENTIAL_THRESHOLD = 1000;

	public NegTask(int s, int e, RGB[] pix, RGB[] d, int w, int h, int m){
		start=s;
		end=e;
		source=pix;
		dest=d;
		width=w;
		height=h;
		max=m;
	}
	
	public void compute(){
		if(end-start<SEQUENTIAL_THRESHOLD){
                	for (int i=start; i<end; i++) {
                	        dest[i]=new RGB(max-source[i].R, max-source[i].G,
                	                          max-source[i].B);
                	}
                	return;
        	}
        	else{
                	int mid=(start+end)/2;
                	NegTask left = new NegTask(start, mid, source, dest, width, height, max);
                	NegTask right = new NegTask(mid, end, source, dest, width, height, max);
                	left.fork();
			right.compute();
			left.join();
                	return;
        	}
	}
}

class MirrorTask extends RecursiveAction{
        private int start;
        private int end;
        private RGB[] source;
        private RGB[] dest;
        private int width;
        private int height;
        private int max;

        protected static final int SEQUENTIAL_THRESHOLD = 100;

        public MirrorTask(int s, int e, RGB[] pix, RGB[] d, int w, int h, int m){
                start=s;
                end=e;
                source=pix;
                dest=d;
                width=w;
                height=h;
                max=m;
        }

        public void compute(){
                if(end-start<SEQUENTIAL_THRESHOLD){
			RGB leftside;
			RGB rightside;
			RGB[][] temp=new RGB[height][width];
			//convert to 2d array
                        for(int r=start; r<end; r++){
				for(int c=0; c<width; c++){
					temp[r][c]=source[(r*width)+c];
				}
			}
			//reverse rows
			for(int r=start; r<end; r++){
                                for(int c=0; c<width/2; c++){
                                        RGB val=temp[r][c];
    					temp[r][c] = temp[r][width-c-1];
    					temp[r][width-c-1]=val;
                                }
                        }
			//convert back to 1d array
			for(int r=start; r<end; r++){
                                for(int c=0; c<width; c++){
                                        dest[(r*width)+c]=temp[r][c];
                                }
                        }

                        return;
                }
                else{
                        int mid=(start+end)/2;
                        MirrorTask left = new MirrorTask(start, mid, source, dest,
							width, height, max);
                        MirrorTask right = new MirrorTask(mid, end, source, dest,
							width, height, max);
                        left.fork();
                        right.compute();
                        left.join();
                        return;
                }
        }
}

class BlurTask extends RecursiveAction{
        private int start;
        private int end;
        private RGB[] source;
        private RGB[] dest;
        private int width;
        private int height;
        private int max;
	private double[][] gauss;
	private int radius;

        protected static final int SEQUENTIAL_THRESHOLD = 1000;

        public BlurTask(int s, int e, RGB[] pix, RGB[] d, int w, int h, int m, double[][] g){
                start=s;
                end=e;
                source=pix;
                dest=d;
                width=w;
                height=h;
                max=m;
		gauss=g;
		radius=gauss[0].length/2;
        }

        public void compute(){
                if(end-start<SEQUENTIAL_THRESHOLD){
                        double rblur;
			double gblur;
			double bblur;
			int rt;
			int gt;
			int bt;
			int cw;
			int ch;
			for (int i=start; i<end; i++) {
			  for(int j=radius; j<width-radius; j++){
			    rblur=0.0;
			    gblur=0.0;
			    bblur=0.0;
			    for(int di=0-radius; di<radius+1; di++){
			      for(int dj=0-radius; dj<radius+1; dj++){
                                if(i+di<0){ch=0;}
				else if(i+di>height){ch=height;}
				else{ch=i+di;}
				if(j+dj<0){cw=0;}
				else if(j+dj>width){cw=width;}
				else{cw=j+dj;}
				rblur+=source[ch*width+cw].R*gauss[di+radius][dj+radius];
			        gblur+=source[ch*width+cw].G*gauss[di+radius][dj+radius];
				bblur+=source[ch*width+cw].B*gauss[di+radius][dj+radius];
			      }
			    }
			    rt=(int)(Math.round(rblur));
			    gt=(int)(Math.round(gblur));
			    bt=(int)(Math.round(bblur));
			    dest[i*width+j]=new RGB(rt,gt,bt);
                          }
			}
                        return;
                }
                else{
                        int mid=(start+end)/2;
                        BlurTask left = new BlurTask(start, mid, source, dest,
							width, height, max, gauss);
                        BlurTask right = new BlurTask(mid, end, source, dest,
							width, height, max, gauss);
                        left.fork();
                        right.compute();
                        left.join();
                        return;
                }
        }
}



class test{
	public static void main(String[] args){
		
		PPMImage florence=new PPMImage(0,0,0,null);
		try {
			florence = PPMImage.fromFile("florence.ppm");
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
		} catch (IOException e) {
			System.out.println("I/O Error!");
		}
		/*
		PPMImage negate = florence.negate();
		try {
			negate.toFile("negate.ppm");
		} catch (IOException e) {
			System.out.println("I/O Error!");
		}		
	
		PPMImage mirror = florence.mirrorImage();
		try {
			mirror.toFile("mirror.ppm");
		} catch (IOException e) {
			System.out.println("I/O Error!");
		}
		*/
		PPMImage blur = florence.gaussianBlur(50, 25);
		try {
			blur.toFile("blur.ppm");
		} catch (IOException e) {
			System.out.println("I/O Error!");
		}
		/*
		try {
			florence.toFile("original.ppm");
		} catch (IOException e) {
			System.out.println("I/O Error!");
		}
		*/
	}
}

