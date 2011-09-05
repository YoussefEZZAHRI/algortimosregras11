package problema;


import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import pareto.FronteiraPareto;

import solucao.ComparetorObjetivo;
import solucao.Solucao;
import solucao.SolucaoNumerica;

/**
 * Classe que representa o problema DTLZ1
 * @author andre
 *
 */

public class DTLZ1 extends Problema {

	/**
	 * Construtor da classe
	 * @param m Numero de objetivos do problema
	 */
	
	
	
	public DTLZ1(int m){
		super(m);
		problema = "dtlz1";
	}
	
	/**
	 * Metodo que calcula os objetivos da solucao passada como parametro
	 * Equacao 7 do artigo "Scalable Multi-Objective Optimization Test Problems"
	 */
	public double[] calcularObjetivos(Solucao sol) {
		SolucaoNumerica solucao = (SolucaoNumerica) sol;
		if(solucao.objetivos == null)
		   solucao.objetivos = new double[m];
		
		double g = g1(solucao.xm);
		//System.out.println(g);

		for(int i = 0; i<m; i++){
			double fxi = 0.5*(1+g);
			int j;
			for(j = 0; j<(m-1-i);j++){
				fxi*=solucao.getVariavel(j);
			}
			if(j!=m-1){
			  fxi *= (1-solucao.getVariavel(j));
			}
			solucao.objetivos[i] = fxi;
		}
		
		avaliacoes++;
		return solucao.objetivos;
	}
	
		
		
	public  ArrayList<SolucaoNumerica> obterFronteira(int n, int numSol){
			
		Random rand = new Random();
		rand.setSeed(1000);
		
		double ocupacao = 0;
		
		FronteiraPareto pareto = new FronteiraPareto(s, maxmim, r, ocupacao,0);
		
		while(pareto.getFronteira().size()<numSol){
			SolucaoNumerica melhor = new SolucaoNumerica(n, m);

			for (int i = m-1; i <n; i++) {
				melhor.setVariavel(i, 0.5);
			}

			for (int i = 0; i < m-1; i++) {
				double newVal = rand.nextDouble();
				melhor.setVariavel(i, newVal);
			}

			double somaParcial = 0;
			calcularObjetivos(melhor);

			for (int i = 0; i < melhor.m; i++) {
				somaParcial += melhor.objetivos[i];
			}
			if(somaParcial==0.5){
				if(!pareto.getFronteira().contains(melhor))
					pareto.add(melhor);	
			}
			
		}
			
		ArrayList<SolucaoNumerica> saida = new ArrayList<SolucaoNumerica>();
		for (Iterator<Solucao> iterator = pareto.getFronteira().iterator(); iterator.hasNext();) {
			SolucaoNumerica solucaoNumerica = (SolucaoNumerica) iterator.next();
			saida.add(solucaoNumerica);
		}
		
			
		return saida;	
	}
	
	public  ArrayList<SolucaoNumerica> obterFronteiraIncremental(int n){
		
		ArrayList<SolucaoNumerica> melhores = new ArrayList<SolucaoNumerica>();
		
		Random rand = new Random();
		rand.setSeed(1000);
		
		//Indicies que indicam que variaves serão geradas incrementalmente para a geracao da fronteira
		//O padrao dos problemas DTLZ eh entre 0 e m-2
		int inicio = 0;
		int fim = m-2;
		
		SolucaoNumerica solucaoBase = new SolucaoNumerica(n, m);
		
		varVez = fim;
		
		for (int i = 0; i < solucaoBase.getVariaveis().length; i++) {
			solucaoBase.setVariavel(i, 0);
		}
		
		boolean haSolucao = true;
		
		while(haSolucao){
			
			SolucaoNumerica melhor = (SolucaoNumerica) solucaoBase.clone();
			
				
			for (int i = m-1; i <n; i++) {
				melhor.setVariavel(i, 0.5);
			}

			for (int i = 0; i < m-1; i++) {
				double newVal = rand.nextDouble();
				melhor.setVariavel(i, newVal);
			}

			double somaParcial = 0;
			calcularObjetivos(melhor);

			for (int i = 0; i < melhor.m; i++) {
				somaParcial += melhor.objetivos[i];
			}
			if(somaParcial==0.5){
				melhores.add(melhor);	
			}
				
			haSolucao = getProximaSolucao(solucaoBase, inicio, fim);
							
		}

	
		
		return melhores;	
	}
	
	
	public static void main(String[] args) {

		int m = 2;
		//int numSol = 1000;
		int k = 10;
		int n = m + k - 1;
		
		int decimalPlace = 5;
		DTLZ1 dtlz1 = new DTLZ1(m);
		
		dtlz1.inc = 0.001;
		
		//dtlz7.obterFronteira2(n, numSol);
		
		
		ArrayList<SolucaoNumerica> f = dtlz1.obterFronteiraIncremental(n);
		//ArrayList<SolucaoNumerica> f = dtlz1.obterFronteira(n, numSol);
		ComparetorObjetivo comp = new ComparetorObjetivo(0);
		Collections.sort(f,comp);
		
		try{
			PrintStream ps = new PrintStream("fronteiras/fronteira_dtlz1_inc" + m);
			PrintStream psSol = new PrintStream("fronteiras/solucoes_dtlz1_inc" + m);
			for (Iterator<SolucaoNumerica> iterator = f.iterator(); iterator.hasNext();) {
				SolucaoNumerica solucaoNumerica = (SolucaoNumerica) iterator
						.next();
			
				
				for(int i = 0; i<m; i++){
					BigDecimal bd = new BigDecimal(solucaoNumerica.objetivos[i]);     
					bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
					ps.print( bd+ " ");
				}
				ps.println();
				
				for(int i = 0; i<solucaoNumerica.getVariaveis().length; i++){
					psSol.print(solucaoNumerica.getVariavel(i) + " ");
				}
				
				psSol.println();
				
			}
		} catch (IOException ex){ex.printStackTrace();}
	}
	

}