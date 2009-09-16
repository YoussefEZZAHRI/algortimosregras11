package pareto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import kernel.nuvemparticulas.ComparetorRankParticula;
import kernel.nuvemparticulas.Particula;

import solucao.ComparetorCrowdDistance;
import solucao.ComparetorCrowdedOperator;
import solucao.ComparetorRank;
import solucao.Solucao;
import sun.java2d.pipe.SolidTextRenderer;

public class FronteiraPareto {
	
	public ArrayList<Solucao> fronteira = null;
	
	public ArrayList<Particula> fronteiraNuvem = null;
	
	public double S;
	
	public boolean modificar = false;
	
	public FronteiraPareto(double s, boolean mod){
		fronteira = new ArrayList<Solucao>();
		fronteiraNuvem = new ArrayList<Particula>();
		S = s;
		modificar = mod;
	}
	
	public void setFronteira(ArrayList<Solucao> temp){
		fronteira.clear();
		for (Iterator<Solucao> iter = temp.iterator(); iter.hasNext();) {
			Solucao s = (Solucao) iter.next();
			fronteira.add(s);
			
		}
	}
	
	public void setFronteiraNuvem(ArrayList<Particula> temp){
		fronteiraNuvem.clear();
		for (Iterator<Particula> iter = temp.iterator(); iter.hasNext();) {
			Particula p = (Particula) iter.next();
			fronteiraNuvem.add(p);
			
		}
	}
	
	public void apagarFronteira(){
		fronteira.clear();
	}
	
	public void apagarFronteiraNuvem(){
		fronteiraNuvem.clear();
	}
	
	/**
	 * M�todo que adiciona um nova solu��o na fronteira de pareto
	 * @param regra Regra a ser adicionada
	 * @return Valor booleano que especifica se o elemento foi inserido ou nao na fronteira 
	 */
	public double add(Solucao solucao){
		//S� adiciona na fronteira caso a regra seja da classe passada como parametro
		solucao.numDominacao = 0;
		if(fronteira.size()==0){
			fronteira.add(solucao);
			return solucao.numDominacao;
		}
		
		int comp;
		
		ArrayList<Solucao> cloneFronteira = (ArrayList<Solucao>)fronteira.clone();
		
		double[] novosObjetivosSolucao = new double[solucao.objetivos.length];
		if(modificar){
			double r = r(solucao.objetivos);
			for (int i = 0; i < solucao.objetivos.length; i++) {
				novosObjetivosSolucao[i] = modificacaoDominanciaPareto(solucao.objetivos[i], r, S);
			}}
		else
			novosObjetivosSolucao = solucao.objetivos;
		
		for (Iterator iter = cloneFronteira.iterator(); iter.hasNext();) {
			Solucao temp = (Solucao) iter.next();
			
			double[] novosObjetivosTemp = new double[temp.objetivos.length];
			if(modificar){
				double r = r(temp.objetivos);
				for (int i = 0; i < temp.objetivos.length; i++) {
					novosObjetivosTemp[i] = modificacaoDominanciaPareto(temp.objetivos[i], r, S);
				}
			}
			else
				novosObjetivosTemp = temp.objetivos;
			
			comp = compararMedidas(novosObjetivosSolucao, novosObjetivosTemp);
			if(comp == -1)
				solucao.numDominacao++;
			if(comp == 1)
				fronteira.remove(temp);
			
		}
		if(solucao.numDominacao == 0){
			fronteira.add(solucao);	
		}
		
		return solucao.numDominacao;
		
	}
	
	
	public double add(Particula particula){
		//S� adiciona na fronteira caso a regra seja da classe passada como parametro
		particula.solucao.numDominacao = 0;
		if(fronteiraNuvem.size()==0){
			fronteiraNuvem.add(particula);
			return particula.solucao.numDominacao;
		}
		
		int comp;
		
		ArrayList<Particula> cloneFronteira = (ArrayList<Particula>)fronteiraNuvem.clone();
		
		Solucao solucao = particula.solucao;
		double[] novosObjetivosSolucao = new double[solucao.objetivos.length];
		if(modificar){
			double r = r(solucao.objetivos);
			for (int i = 0; i < solucao.objetivos.length; i++) {
				novosObjetivosSolucao[i] = modificacaoDominanciaPareto(solucao.objetivos[i], r, S);
			}}
		else
			novosObjetivosSolucao = solucao.objetivos;
		
		for (Iterator iter = cloneFronteira.iterator(); iter.hasNext();) {
			Particula temp = (Particula) iter.next();
			
			double[] novosObjetivosTemp = new double[temp.solucao.objetivos.length];
			if(modificar){
				double r = r(temp.solucao.objetivos);
				for (int i = 0; i < temp.solucao.objetivos.length; i++) {
					novosObjetivosTemp[i] = modificacaoDominanciaPareto(temp.solucao.objetivos[i], r, S);
				}
			}
			else
				novosObjetivosTemp = temp.solucao.objetivos;
			
			comp = compararMedidas(novosObjetivosSolucao, novosObjetivosTemp);
			if(comp == -1){
				particula.solucao.numDominacao++;
			}
			if(comp == 1)
				fronteiraNuvem.remove(temp);
			
		}
		if(particula.solucao.numDominacao==0){
			fronteiraNuvem.add(particula);
			return particula.solucao.numDominacao;
		} else{
			return particula.solucao.numDominacao;
		}
	}
	
	public void podarLideresCrowd(int tamanhoRepositorio){
		if(tamanhoRepositorio<fronteiraNuvem.size()){
			ComparetorCrowdDistance comp = new ComparetorCrowdDistance();
			//ComparetorCrowdedOperator comp = new ComparetorCrowdedOperator();
			Collections.sort(fronteiraNuvem, comp);
			int diferenca = fronteiraNuvem.size() - tamanhoRepositorio; 
			for(int i = 0; i<diferenca; i++)
				fronteiraNuvem.remove(fronteiraNuvem.remove(fronteiraNuvem.size()-1));
			retornarFronteiraNuvem();
		}
	}
	
	public void podarLideresRank(int tamanhoRepositorio){
		if(tamanhoRepositorio<fronteiraNuvem.size()){
			ComparetorRankParticula	comp = new ComparetorRankParticula();
			Collections.sort(fronteiraNuvem, comp);
			int diferenca = fronteiraNuvem.size() - tamanhoRepositorio; 
			for(int i = 0; i<diferenca; i++)
				fronteiraNuvem.remove(fronteiraNuvem.remove(fronteiraNuvem.size()-1));
			retornarFronteiraNuvem();
		}
	}
	
	
	
	
	public void retornarFronteiraNuvem(){
		fronteira.clear();
		for (Iterator iterator = fronteiraNuvem.iterator(); iterator.hasNext();) {
			Particula particula = (Particula) iterator.next();
			fronteira.add(particula.solucao);
			
		}
	}
	
	
	
	public ArrayList<Solucao> getFronteira(){
		return fronteira;
	}
	
	public String toString(){
		return fronteira.toString();
	}
	/**
	 * M�todo que verifica se uma solu��o domina a outra (Minimiza��o)
	 * @param sol1 Solu��o que ser� comparada com as regras pertencentes a fronteira de pareto
	 * @param sol2 Solu��o pertencente a fronteira de pareto
	 * @return -1 Se sol1 for dominada, 0 se a sol1 nao domina nem eh dominada, 1 sol1 domina sol2 
	 */
	public static int compararMedidas(double[] sol1, double[] sol2){
		//Contador que marca quantos valores da regra 1 sao maiores que os da regra2
		//Se cont for igual ao tamanho dos elementos da regra 1 entao a regra 2 eh dominada pela regra1
		//Se cont for igual a 0 a regra2 domina a regra1
		//Se cont for maior do que 0 e menor que o tamanho ela nao domina e nem eh dominada
		int cont = 0; 
		int cont2 = sol1.length;
		for (int i = 0; i < sol1.length; i++) {
			if(sol1[i]<sol2[i]){
				++cont;
			} else {
				if(sol1[i]==sol2[i]){
					--cont2;
				}
			}
		}
		if(cont == 0){	
			if(cont2 == 0)
				return 0;
			else
				return -1;
		}
		else{
			if(cont>0 && cont<cont2)
				return 0;
			else return 1;
		}
	}
	
	/**
	 * Modifica��o da modifica��o da domin�ncia de Pareto proposta por Sato
	 * Deriva��o do Sen do Wi atrav�s do Cos
	 * @param fix Valor original da fun��o de objetivo de �ndice i
	 * @param r Norma do vetor de objetivos
	 * @param si Par�metro da modifica��o da dominacia (Varia entre 0 e 1)
	 * @return
	 */
	public double modificacaoDominanciaPareto(double fix, double r, double si){
		double cosWi = fix/r;
		double cosWi2 = cosWi*cosWi;
		double senWi = Math.sqrt(1-cosWi2);
		double senSiPi = Math.sin(si * Math.PI);
		double cosSiPi = Math.cos(si * Math.PI);
		//Formula: r*sen(Wi+SiPi)/sen(SiPi)
		double numerador = r*((senWi*cosSiPi)+(cosWi*senSiPi));
		double novoFix = numerador/senSiPi;
		return Math.max(novoFix, 0);
	}
	
	public double r(double[] objetivos){
		double soma = 0;
		for (int i = 0; i < objetivos.length; i++) {
			double fix = objetivos[i];
			soma+=fix*fix;
		}
		return Math.sqrt(soma);
	}
	
	/**
	 * M�todo que retorna de quantas solu��es a solu��o passada como par�metro � dominada
	 * @param solucao Solu��o a ser contada o numero de domina��o		
	 * @return Quantas solu��es a solu��o passada como parametro � dominada
	 */
	public double obterNumDomincao(Solucao solucao, ArrayList<Solucao> solucoes){
		
		int numDominacao = 0;
		
		int comp;
	
		double[] novosObjetivosSolucao = new double[solucao.objetivos.length];
		if(modificar){
			double r = r(solucao.objetivos);
			for (int i = 0; i < solucao.objetivos.length; i++) {
				novosObjetivosSolucao[i] = modificacaoDominanciaPareto(solucao.objetivos[i], r, S);
			}}
		else
			novosObjetivosSolucao = solucao.objetivos;
		
		for (Iterator<Solucao> iter = solucoes.iterator(); iter.hasNext();) {
			Solucao temp = (Solucao) iter.next();
			
			double[] novosObjetivosTemp = new double[temp.objetivos.length];
			if(modificar){
				double r = r(temp.objetivos);
				for (int i = 0; i < temp.objetivos.length; i++) {
					novosObjetivosTemp[i] = modificacaoDominanciaPareto(temp.objetivos[i], r, S);
				}
			}
			else
				novosObjetivosTemp = temp.objetivos;
			
			comp = compararMedidas(novosObjetivosSolucao, novosObjetivosTemp);
			if(comp == -1)
				numDominacao ++;
		}
		
		return numDominacao;
	}

}
