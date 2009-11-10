package kernel.nuvemparticulas;


import java.util.ArrayList;
import java.util.Iterator;


import problema.DTLZ2;
import problema.Problema;
import solucao.Solucao;
import kernel.Avaliacao;

/**
 * Classe que implementa o algoritmo da Otimiza��o por nuvem de part�culas multi-objetivo.
 * @author Andr� B. de Carvalho
 *
 */
public class SigmaMOPSO extends MOPSO{


	
	public SigmaMOPSO(int n, Problema prob, int g, int t, double s, boolean mod){
		super(n,prob,g,t, s, mod);

	}
	

	/**
	 * M�todo principal que executa as opera�oes do MOPSO
	 */
	public ArrayList<Solucao> executar(){
		//Apaga todas as listas antes do inicio da execu��o
		reiniciarExecucao();
		//Inicia a popul�ao
		inicializarPopulacao();
		//Obt�m as melhores part�culas da popula��o
		atualizarRepositorio();		
		//Obt�m os melhores globais para todas as part�culas da popula��o
		escolherLideres();
		//In�cia o la�o evolutivo
		for(int i = 0; i<geracoes; i++){
			if(i%10 == 0)
				System.out.print(i + " - " + geracoes + " ");
			//Itera sobre todas as part�culas da popula��o
 			for (Iterator<Particula> iter = populacao.iterator(); iter.hasNext();) {
				Particula particula = (Particula) iter.next();
				//Calcula a nova velocidade
				particula.calcularNovaVelocidade(i);
				//Calcula a nova posi��o
				particula.calcularNovaPosicao();
				//Turbul�ncia na posi��o da part�cula
				mutacao(0.05, particula.posicao);
				particula.truncar();
				//Avalia a part�cula
				problema.calcularObjetivos(particula.solucao);
				//Define o melhor local
				particula.escolherLocalBest();
			}
 			//Obt�m as melhores particulas da popula��o
			atualizarRepositorio();
			//Escolhe os novos melhores globais
			escolherLideres();
		}
		
		
		pareto.retornarFronteiraNuvem();
		return pareto.getFronteira();
	}
	
	/**
	 * M�todo que escolhe para cada particula da populacao uma particula presente no repositorio
	 *
	 */
	public void escolherLideres(){
		for (Iterator<Particula> iter = pareto.fronteiraNuvem.iterator(); iter.hasNext();) {
			Particula partRepositorio =  iter.next();
			partRepositorio.calcularSigmaVector();
		}
		
		for (Iterator<Particula> iter = populacao.iterator(); iter.hasNext();) {
			Particula particula = iter.next();
			particula.calcularSigmaVector();
			particula.escolherGlobalBestSigma(pareto.fronteiraNuvem);
		}
	}
	
	
	public static void main(String[] args) {
		int m = 3;
		Problema prob = new DTLZ2(m);
		int n = 5;
		int g = 50;
		int t = 100;
		for(int i = 0; i<5; i++){
			SigmaMOPSO nuvem = new SigmaMOPSO(n, prob, g, t, 0.25, false);
			ArrayList<Solucao> fronteira= nuvem.executar();
			for (Iterator<Solucao> iterator = nuvem.pareto.fronteira.iterator(); iterator.hasNext();) {
				Solucao solucao = (Solucao) iterator.next();
				prob.calcularObjetivos(solucao);
				//System.out.println(solucao);
				
			}
			//System.out.println();
			Avaliacao aval = new Avaliacao(fronteira, m);
			aval.avaliar();	
		}
		
		
		
	}
	

}