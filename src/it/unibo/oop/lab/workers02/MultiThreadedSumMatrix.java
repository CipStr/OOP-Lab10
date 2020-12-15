package it.unibo.oop.lab.workers02;

import java.util.ArrayList;
import java.util.List;


public final class MultiThreadedSumMatrix implements SumMatrix {
	
	private final int nthread;
	
	public MultiThreadedSumMatrix (final int nthread) {
		this.nthread=nthread;
	}
	
	private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int nelem;
        private long res;
        
        Worker(final double[][] matrix, final int startpos, final int nelem) {
            super();
            this.matrix = matrix.clone();
            this.startpos = startpos;
            this.nelem = nelem;
        }
	
        public void run() {
        	int finalIndexValue = this.startpos + this.nelem;
            if (finalIndexValue > matrix.length) {
                finalIndexValue = matrix.length;
            }
            System.out.println("Working from matrix[" + this.startpos + "][] to matrix[" + (finalIndexValue - 1) + "][]");
            for (int i = startpos; i < finalIndexValue; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    this.res += matrix[i][j];
                }
            }
        }
        
        public long getResult() {
            return this.res;
        }
	}
	 
	@Override
	public double sum(final double[][] matrix) {
		 final int size = matrix.length % nthread + matrix.length / nthread;
		 final List<Worker> workers = new ArrayList<>(nthread);
	        for (int start = 0; start < matrix.length; start += size) {
	            workers.add(new Worker(matrix, start, size));
	        }
	        /*
	         * Start them
	         */
	        for (final Worker w: workers) {
	            w.start();
	        }
	        /*
	         * Wait for every one of them to finish. This operation is _way_ better done by
	         * using barriers and latches, and the whole operation would be better done with
	         * futures.
	         */
	        long sum = 0;
	        for (final Worker w: workers) {
	            try {
	                w.join();
	                sum += w.getResult();
	            } catch (InterruptedException e) {
	                throw new IllegalStateException(e);
	            }
	        }
	        /*
	         * Return the sum
	         */
	        return sum;
	    }

}
