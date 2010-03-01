package problema;

import java.util.ArrayList;
import java.util.Random;

import pareto.FronteiraPareto;
import solucao.Solucao;
import solucao.SolucaoNumerica;

/**
 * Classe que representa o problema DTLZ2
 * @author andre
 *
 */



public class DTLZ4 extends Problema {


	public static final double alpha = 100;
	/**
	 * Construtor da classe
	 * @param m Numero de objetivos do problema
	 */
	public DTLZ4(int m){
		super(m);
	}
	
	/**
	 * Metodo que calcula os objetivos da solucao passada como parametro
	 * Equacao 9 do artigo "Scalable Multi-Objective Optimization Test Problems"
	 */
	public double[] calcularObjetivos(Solucao sol) {
		SolucaoNumerica solucao = (SolucaoNumerica) sol;
		if(solucao.objetivos == null)
		   solucao.objetivos = new double[m];
		
		double g = g2(solucao.xm);
		double pi_2 = Math.PI/2.0;
		//System.out.print("f(0): ");
		double xiAlpha = Math.pow(solucao.getVariavel(0), alpha);
		double f0 = (1+g)*Math.cos(xiAlpha*pi_2);
		//System.out.print("Cos 0 ");
		for(int i = 1; i<m-1; i++){
			f0 *= Math.cos(solucao.getVariavel(i)*pi_2);
			//System.out.print("Cos " + i  + " ");
		}
	   solucao.objetivos[0] = f0;
		for(int i = 1; i<(m); i++){
			//System.out.print("f(" + i + "): ");
			double fxi = (1+g);
			int j = 1;
			
			for(j = 0; j<(m-1-i);j++){
				xiAlpha = Math.pow(solucao.getVariavel(j), alpha);
				fxi*=(Math.cos(xiAlpha*pi_2));
				//System.out.print("Cos " + j  + " ");
			}
			xiAlpha = Math.pow(solucao.getVariavel(j), alpha);
			fxi *= (Math.sin(xiAlpha*pi_2));
			//System.out.print("Sen " + j  + " ");
			//System.out.println();
			solucao.objetivos[i] = fxi;
		}
		avaliacoes++;
		return solucao.objetivos;
	}
	
	public  ArrayList<SolucaoNumerica> obterFronteira(int n, int numSol){
		ArrayList<SolucaoNumerica> melhores = new ArrayList<SolucaoNumerica>();
		
		Random rand = new Random();
		rand.setSeed(1000);
		
		while(melhores.size()<numSol){
			SolucaoNumerica melhor = new SolucaoNumerica(n, m);

			for (int i = m-1; i <n; i++) {
				melhor.setVariavel(i, 0.5);
			}

			for (int i = 0; i < m-1; i++) {
				double newVal = rand.nextDouble();
				melhor.setVariavel(i, newVal);
			}

			
			calcularObjetivos(melhor);
			melhores.add(melhor);
			
		}
		
		return melhores;	
	}
	

	
	public static void main(String[] args) {
		
		int m = 2;
		FronteiraPareto pareto = new FronteiraPareto(0.25);
		
		DTLZ4 dtlz2 = new DTLZ4(m);
		for(int i = 0; i<2; i++){
			SolucaoNumerica sol = new SolucaoNumerica(5, m);
			sol.iniciarSolucaoAleatoria();
			dtlz2.calcularObjetivos(sol);
			System.out.println(sol);
			pareto.add(sol);
		}
		System.out.println("Fronteira:");
		System.out.println(pareto);
		
		
		
	}

}