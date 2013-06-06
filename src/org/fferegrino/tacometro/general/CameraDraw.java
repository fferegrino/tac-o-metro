package org.fferegrino.tacometro.general;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CameraDraw extends View {
	Bitmap mBitmap;
	Paint letrasBlancas;
	Paint bgNegro;
	Paint bgBlanco;
	byte[] mYUVData;
	int[] mRGBData;
	int mImageWidth, mImageHeight;
	int[] mRedHistogram;
	int[] mGreenHistogram;
	int[] mBlueHistogram;

	int segundos;

	boolean preserveValues;
	double[] mBinSquared;
	double whiteBalance;

	private boolean escuchando;
	private double promedio;
	private double variacionMaxima;
	private int pasos;

	public boolean isEscuchando() {
		return escuchando;
	}

	public void setEscuchando(boolean escuchando) {
		this.escuchando = escuchando;
	}

	public double getPromedio() {
		return promedio;
	}

	public void setPromedio(double promedio) {
		this.promedio = promedio;
	}

	public double getVariacionMaxima() {
		return variacionMaxima;
	}

	public void setVariacionMaxima(double variacionMaxima) {
		this.variacionMaxima = variacionMaxima;
	}

	public int getPasos() {
		return pasos;
	}

	public void setPasos(int pasos) {
		this.pasos = pasos;
	}
	
	public void setSegundos(int segundos) {
		this.segundos = segundos;
	}

	public int getSegundos() {
		return this.segundos;
	}

	public CameraDraw(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		letrasBlancas = new Paint();
		letrasBlancas.setStyle(Paint.Style.FILL);
		letrasBlancas.setColor(Color.WHITE);
		letrasBlancas.setTextSize(40);

		bgNegro = new Paint();
		bgNegro.setStyle(Paint.Style.FILL);
		bgNegro.setColor(Color.BLACK);
		
		bgBlanco = new Paint();
		bgBlanco.setStyle(Paint.Style.FILL);
		bgBlanco.setColor(Color.WHITE);

		mBitmap = null;
		mYUVData = null;
		mRGBData = null;
		mRedHistogram = new int[256];
		mGreenHistogram = new int[256];
		mBlueHistogram = new int[256];
		mBinSquared = new double[256];
		for (int bin = 0; bin < 256; bin++) {
			mBinSquared[bin] = ((double) bin) * bin;
		}

		setVariacionMaxima(10);
		setPromedio(75);
		setEscuchando(true);
	}

	public CameraDraw(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CameraDraw(Context context) {
		this(context, null, 0);
	}

	double imageRedStdDev, imageGreenStdDev, imageBlueStdDev;

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			int canvasWidth = canvas.getWidth();
			int canvasHeight = canvas.getHeight();
			int newImageWidth = canvasWidth;
			int newImageHeight = canvasHeight;
			int marginWidth = (canvasWidth - newImageWidth) / 2;
			int middleX = canvasWidth / 2;
			int middleY = canvasHeight / 2;

			// Convert from YUV to RGB
			decodeYUV420SP(mRGBData, mYUVData, mImageWidth, mImageHeight);

			// Calculate histogram
			calculateIntensityHistogram(mRGBData, mRedHistogram, mImageWidth,
					mImageHeight, 0);
			calculateIntensityHistogram(mRGBData, mGreenHistogram, mImageWidth,
					mImageHeight, 1);
			calculateIntensityHistogram(mRGBData, mBlueHistogram, mImageWidth,
					mImageHeight, 2);

			// Calculate mean
			double imageRedMean = 0, imageGreenMean = 0, imageBlueMean = 0;
			double redHistogramSum = 0, greenHistogramSum = 0, blueHistogramSum = 0;
			for (int bin = 0; bin < 256; bin++) {
				imageRedMean += mRedHistogram[bin] * bin;
				redHistogramSum += mRedHistogram[bin];
				imageGreenMean += mGreenHistogram[bin] * bin;
				greenHistogramSum += mGreenHistogram[bin];
				imageBlueMean += mBlueHistogram[bin] * bin;
				blueHistogramSum += mBlueHistogram[bin];
			} // bin
			imageRedMean /= redHistogramSum;
			imageGreenMean /= greenHistogramSum;
			imageBlueMean /= blueHistogramSum;

			// Calculate second moment
			double imageRed2ndMoment = 0, imageGreen2ndMoment = 0, imageBlue2ndMoment = 0;

			for (int bin = 0; bin < 256; bin++) {
				imageRed2ndMoment += mRedHistogram[bin] * mBinSquared[bin];
				imageGreen2ndMoment += mGreenHistogram[bin] * mBinSquared[bin];
				imageBlue2ndMoment += mBlueHistogram[bin] * mBinSquared[bin];
			} // bin
			
			imageRed2ndMoment /= redHistogramSum;
			imageGreen2ndMoment /= greenHistogramSum;
			imageBlue2ndMoment /= blueHistogramSum;
			imageRedStdDev = Math.sqrt(imageRed2ndMoment - imageRedMean
					* imageRedMean);
			imageGreenStdDev = Math.sqrt(imageGreen2ndMoment - imageGreenMean
					* imageGreenMean);
			imageBlueStdDev = Math.sqrt(imageBlue2ndMoment - imageBlueMean
					* imageBlueMean);


			whiteBalance = getWhiteBalance(imageRedStdDev, imageGreenStdDev,
					imageBlueStdDev);

			if (getPromedio() - whiteBalance > getVariacionMaxima()) {
				if (isEscuchando()) {
					setEscuchando(false);
					setPasos(getPasos() + 1);
				}
			} else {
				setEscuchando(true);
			}

			String whiteBalanceS = String.format("Brillo: %3.0f", whiteBalance);

			canvas.drawRect(0, 0, (float) canvasWidth, 50, bgNegro);
			canvas.drawRect(0, canvasHeight, (float) canvasWidth, canvasHeight - 40, bgBlanco);

			canvas.drawText(whiteBalanceS, 0, 40, letrasBlancas);

			canvas.drawText("Paso: " + getPasos(), (canvasWidth / 2) - (letrasBlancas.measureText("Paso: " + getPasos()) / 2), 40, letrasBlancas);

			String seconds = String.format("Seg: %d", segundos);
			canvas.drawText(seconds,
					canvasWidth - 10 - letrasBlancas.measureText(seconds), 40,
					letrasBlancas);

		}

		super.onDraw(canvas);

	}// end onDraw method

	static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height) {
		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j / 2) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;
				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	static public void decodeYUV420SPGrayscale(int[] rgb, byte[] yuv420sp,
			int width, int height) {
		final int frameSize = width * height;

		for (int pix = 0; pix < frameSize; pix++) {
			int pixVal = (0xff & ((int) yuv420sp[pix])) - 16;
			if (pixVal < 0)
				pixVal = 0;
			if (pixVal > 255)
				pixVal = 255;
			rgb[pix] = 0xff000000 | (pixVal << 16) | (pixVal << 8) | pixVal;
		} // pix
	}

	static public void calculateIntensityHistogram(int[] rgb, int[] histogram,
			int width, int height, int component) {
		for (int bin = 0; bin < 256; bin++) {
			histogram[bin] = 0;
		} // bin
		if (component == 0) // red
		{
			for (int pix = 0; pix < width * height; pix += 3) {
				int pixVal = (rgb[pix] >> 16) & 0xff;
				histogram[pixVal]++;
			} // pix
		} else if (component == 1) // green
		{
			for (int pix = 0; pix < width * height; pix += 3) {
				int pixVal = (rgb[pix] >> 8) & 0xff;
				histogram[pixVal]++;
			} // pix
		} else // blue
		{
			for (int pix = 0; pix < width * height; pix += 3) {
				int pixVal = rgb[pix] & 0xff;
				histogram[pixVal]++;
			} // pix
		}
	}

	public double getWhiteBalance(double r, double g, double b) {
		double R = (r * r * (0.299));
		double G = (g * g * (0.587));
		double B = (b * b * (0.114));
		return Math.sqrt(R + G + B);
	}
}