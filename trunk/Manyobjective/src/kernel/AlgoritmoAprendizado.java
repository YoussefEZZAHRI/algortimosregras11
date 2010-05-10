package kernel;



import java.util.ArrayList;

import java.util.Collections;
import java.util.Iterator;

import kernel.nuvemparticulas.Particula;

import pareto.FronteiraPareto;
import problema.Problema;
import solucao.ComparetorObjetivo;
import solucao.ComparetorRank;
import solucao.Solucao;
import solucao.SolucaoBinaria;
import solucao.SolucaoNumerica;

public abstract class AlgoritmoAprendizado {
	
	//N�mero de vari�veis da solu��o
	public int n;
	public Problema problema = null;
	
	//N�mero de execu��es do mopso-n
	public int geracoes;
	//Tamanho inicial da popula��o
	public int tamanhoPopulacao;
	
	public int numeroavalicoes;
	
	public FronteiraPareto pareto = null;
	
	public final double PROB_MUT_COD;
	
	
	private final double MAX_MUT = 0.5;
	//Flag que indica se algum metodo de rankeamento many-objetivo sera utilizado
	public boolean rank = false;
	
	
	public AlgoritmoAprendizado(int n, Problema p, int g, int avaliacoes, int t){
		this.n = n;
		problema = p;
		geracoes = g;
		tamanhoPopulacao = t;
		PROB_MUT_COD = 1.0/(double)n;
		
		numeroavalicoes = avaliacoes;
	}
	
	public abstract ArrayList<Solucao> executar();
	
	public abstract ArrayList<Solucao> executarAvaliacoes();
	
	public double distanciaEuclidiana(double[] vetor1, double[] vetor2){
		double soma = 0;
		for (int i = 0; i < vetor1.length; i++) {
			soma += Math.pow(vetor1[i]-vetor2[i],2);
		}
		return Math.sqrt(soma);
	}
	
	public void calcularCrowdingDistance(ArrayList<Solucao> solucoes){
		for (Iterator<Solucao> iterator = solucoes.iterator(); iterator.hasNext();) {
			Solucao solucao =  iterator.next();
			solucao.crowdDistance = 0;
		}
		
		for(int m = 0; m<problema.m; m++){
			ComparetorObjetivo comp = new ComparetorObjetivo(m);
			Collections.sort(solucoes, comp);
			Solucao sol1 = solucoes.get(0);
			Solucao solN = solucoes.get(solucoes.size()-1);
			sol1.crowdDistance = Double.MAX_VALUE;
			solN.crowdDistance = Double.MAX_VALUE;
			for(int i = 1; i<solucoes.size()-1; i++){
				Solucao sol = solucoes.get(i);
				Solucao solProx = solucoes.get(i+1);
				Solucao solAnt = solucoes.get(i-1);
				sol.crowdDistance += solProx.objetivos[m] - solAnt.objetivos[m];
			}
		}
		
	}
	
	/**
	 * Muta��o probabil�stica
	 * @param prob_mutacao Probabilidade de efetuar a muta��o em uma posi��o
	 * @param vetor1 Vetor que ir� sofre a muta��o
	 */
	public void mutacaoPolinomial(double prob_mutacao, double[] vetor1){
		for (int i = 0; i < vetor1.length; i++) {
			double pos = vetor1[i];
			double prob = Math.random();
			double delta;
			if(prob<prob_mutacao){
				double u = Math.random();
				if(u<0.5){
					delta = Math.pow(2*u, 1.0/(vetor1.length+1)) - 1;
				} else{
					delta = 1- Math.pow(2*(1-u), 1.0/(vetor1.length+1));
				}
				
			} else
				delta = 0;
			vetor1[i] = Math.max(0,pos + delta*MAX_MUT); 
		}
	}
	
	public void mutacao(double prob_mutacao, Solucao solucao){
		if(solucao.isNumerica())
			mutacaoPolinomialNumerica(prob_mutacao, (SolucaoNumerica)solucao);
		else
			((SolucaoBinaria) solucao).mutacaoSimples(prob_mutacao);
	}
	
	/**
	 * Muta��o probabil�stica
	 * @param prob_mutacao Probabilidade de efetuar a muta��o em uma posi��o
	 * @param 
	 */
	public void mutacaoPolinomialNumerica(double prob_mutacao, SolucaoNumerica solucao){
		for (int i = 0; i < solucao.n; i++) {
			double pos = solucao.getVariavel(i);
			double prob = Math.random();
			double delta;
			if(prob<prob_mutacao){
				double u = Math.random();
				if(u<0.5){
					delta = Math.pow(2*u, 1.0/(solucao.n+1)) - 1;
				} else{
					delta = 1- Math.pow(2*(1-u), 1.0/(solucao.n+1));
				}
				
			} else
				delta = 0;
			solucao.setVariavel(i, pos + delta*MAX_MUT); 
		}
	}
	
	/**
	 * M�todo que executa a muta��o simples, pos = pos + random(0,1)*pos
	 * @param prob_mutacao Probabilidade de efetuar a muta��o em uma posi��o
	 * @param vetor1 Vetor que ir� sofre a muta��o
	 */
	public void mutacao(double prob_mutacao, double[] vetor1){
		for (int i = 0; i < vetor1.length; i++) {
			double pos = vetor1[i];
			double prob = Math.random();
			if(prob<prob_mutacao){
				pos += (Math.random() % pos);
				vetor1[i] = pos;
			}
		}	
	}
	
	public void averageRankParticula(ArrayList<Particula> particulas){
		ArrayList<Solucao> solucoes = new ArrayList<Solucao>();
		for (Iterator<Particula> iterator = particulas.iterator(); iterator.hasNext();) {
			Particula particula = (Particula) iterator.next();
			solucoes.add(particula.solucao);
		}
		
		
		averageRank(solucoes);
		solucoes.clear();
		solucoes = null;
	}
	
	public void averageRank(ArrayList<Solucao> solucoes){
		int[][][] A = new int[solucoes.size()][solucoes.size()][problema.m];
		
		for(int i = 0; i<solucoes.size()-1; i++){
			Solucao solucaoi = solucoes.get(i);
			for(int j = i+1; j<solucoes.size(); j++){
				Solucao solucaoj =  solucoes.get(j);
				for(int k = 0; k<problema.m; k++){
					if(solucaoi.objetivos[k]<solucaoj.objetivos[k]){
						A[i][j][k] = 1;
						A[j][i][k] = -1;
					} else {
						if(solucaoi.objetivos[k]>solucaoj.objetivos[k]){
							A[i][j][k] = -1;
							A[j][i][k] = 1;
						} else {
							A[i][j][k] = 0;
							A[j][i][k] = 0;
						}
					}
				}
			}
		}
		
		for(int i = 0; i<solucoes.size(); i++){
			Solucao solucaoi = solucoes.get(i);
			solucaoi.rank = 0;
		}
		
		for(int i = 0; i<solucoes.size(); i++){
			Solucao solucaoi = solucoes.get(i);
			
		for(int k = 0; k<problema.m; k++){
				double rank = 0;
				for(int j = 0; j<solucoes.size(); j++){
					if(i!=j){
						if(A[i][j][k]==1)
							rank++;
					}
				}
				
				double rankObj = (solucoes.size()) - rank;
				solucaoi.rank+= rankObj;
				
				
			}
		
	
		}
		
	}
	
	public void averageRankModificado(ArrayList<Solucao> solucoes){
		int[][][] A = new int[solucoes.size()][solucoes.size()][problema.m];
		
		for(int i = 0; i<solucoes.size()-1; i++){
			Solucao solucaoi = solucoes.get(i);
			for(int j = i+1; j<solucoes.size(); j++){
				Solucao solucaoj =  solucoes.get(j);
				for(int k = 0; k<problema.m; k++){
					if(solucaoi.objetivos[k]<solucaoj.objetivos[k]){
						A[i][j][k] = 1;
						A[j][i][k] = -1;
					} else {
						if(solucaoi.objetivos[k]>solucaoj.objetivos[k]){
							A[i][j][k] = -1;
							A[j][i][k] = 1;
						} else {
							A[i][j][k] = 0;
							A[j][i][k] = 0;
						}
					}
				}
			}
		}
		
		for(int i = 0; i<solucoes.size(); i++){
			Solucao solucaoi = solucoes.get(i);
			solucaoi.rank = 0;
		}
		
		for(int i = 0; i<solucoes.size(); i++){
			Solucao solucaoi = solucoes.get(i);
			double maiorRank = Double.NEGATIVE_INFINITY;
			double menorRank = Double.POSITIVE_INFINITY;
		for(int k = 0; k<problema.m; k++){
				double rank = 0;
				for(int j = 0; j<solucoes.size(); j++){
					if(i!=j){
						if(A[i][j][k]==1)
							rank++;
					}
				}
				
				double rankObj = (solucoes.size()) - rank;
				solucaoi.rank+= rankObj;
				
				if (rankObj >maiorRank)
					maiorRank = rankObj;
				if(rankObj<menorRank)
					menorRank = rankObj;
			}
		
		double diff = (maiorRank - menorRank)/ solucoes.size();

		solucaoi.rank = solucaoi.rank * diff;	
		}
		
	}
	
	

	
	/**
	 * M�todo que busca as solu��es n�o dominadas da popula��o atual
	 * @return Solu��es n�o dominadas da popula��o
	 */
	public void encontrarSolucoesNaoDominadas(ArrayList<Solucao> solucoes, FronteiraPareto pareto){
		for (Iterator<Solucao> iter = solucoes.iterator(); iter.hasNext();) {
			Solucao solucao =  iter.next();
			pareto.add(solucao);
		}
	}
	

	
	
}
