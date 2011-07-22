package kernel.nuvemparticulas;



import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


import pareto.FronteiraPareto;
import problema.DTLZ2;
import problema.Problema;
import solucao.ComparetorObjetivo;
import solucao.Solucao;
import solucao.SolucaoNumerica;


/**
 * Classe que implementa o algoritmo da Otimiza��o por nuvem de part�culas multi-objetivo.
 * @author Andr� B. de Carvalho
 *
 */
public class SMOPSO extends MOPSO{

	
	public final double INDICE_MUTACAO = 0.15;
	
	public int tamanhoRepositorio;
	
	
	//PrintStream psSol;
	
		
	public SMOPSO(int n, Problema prob, int g, int a, int t, double s, String[] maxmim, int tamRep, String tRank, double ocupacao, double fator, double smax){
		super(n,prob,g,a,t,s, maxmim, tRank, ocupacao, fator, smax);
		tamanhoRepositorio = tamRep;	
		
			
		/*try{
			psSol = new PrintStream("solucoes_" + pareto.S);
		}catch(IOException ex){ex.printStackTrace();}*/
	}
		
	/**
	 * M�todo principal que executa as opera�oes do MOPSO
	 */
	public ArrayList<Solucao> executar(){
		
//		teste();
		//Apaga todas as listas antes do inicio da execu��o
		reiniciarExecucao();
		
		//iniciarPopulacaoTeste();
		//rankParticula(populacao);
		
		//Inicia a popul�ao
		inicializarPopulacao();
		//iniciarPopulacaoTeste();
		
		//Obt�m as melhores part�culas da popula��o
		
				
		
					
		if(!rank)
			atualizarRepositorio();
		else
			iniciarRepositorioRank();	
		
		
		//calcularCrowdingDistance(pareto.fronteira);

		//Obt�m os melhores globais para todas as part�culas da popula��o
		escolherLideres();
		
		
		escolherParticulasMutacao();
		//In�cia o la�o evolutivo
		for(int i = 0; i<geracoes; i++){
			if(i%10 == 0)
				System.out.print(i + " ");
			if(i % 100 ==0)
				System.out.println();
			lacoEvolutivo(i);
		}
		
		/*if(rank){

			ArrayList<Particula> cloneFronteira = (ArrayList<Particula>)pareto.fronteiraNuvem.clone();

			pareto.apagarFronteiraNuvem();

			for (Iterator<Particula> iter = cloneFronteira.iterator(); iter.hasNext();) {
				Particula particula =  iter.next();
				particula.problema = this.problema;
				if(!pareto.fronteiraNuvem.contains(particula)){
					particula.solucao.numDominacao = pareto.add((Particula)particula.clone());

				}
			}	

		}*/
		
		//removerGranularRaio(pareto.getFronteira());
		calcularCrowdingDistance(pareto.fronteira);
		pareto.podarLideresCrowdedOperator(tamanhoRepositorio);
		
		return pareto.getFronteira();
		
	}
	
	public ArrayList<Solucao> executarAvaliacoes(){

		//Apaga todas as listas antes do inicio da execu��o
		reiniciarExecucao();
		//Inicia a popul�ao
		inicializarPopulacao();
		//Obt�m as melhores part�culas da popula��o
		if(!rank)
			atualizarRepositorio();
		else
			iniciarRepositorioRank();
		
		//calcularCrowdingDistance(pareto.fronteira);
		//Obt�m os melhores globais para todas as part�culas da popula��o
		escolherLideres();

		escolherParticulasMutacao();
		//In�cia o la�o evolutivo
		while(problema.avaliacoes < numeroavalicoes){
			//if(problema.avaliacoes%1000 == 0)
			//	System.out.print(problema.avaliacoes + " - " + numeroavalicoes + " ");
			lacoEvolutivo(problema.avaliacoes);
		}

		//removerGranularRaio(pareto.getFronteira());
		calcularCrowdingDistance(pareto.fronteira);
		pareto.podarLideresCrowdedOperator(tamanhoRepositorio);
		return pareto.getFronteira();
		
	}

	private void lacoEvolutivo(int i) {
		
		//Itera sobre todas as part�culas da popula��o
		for (Iterator<Particula> iter = populacao.iterator(); iter.hasNext();) {
			Particula particula = (Particula) iter.next();
			//Calcula a nova velocidade
			particula.calcularNovaVelocidadeConstriction();
			//Calcula a nova posi��o
			particula.calcularNovaPosicao();
			if(particula.mutacao){
				mutacaoPolinomial(PROB_MUT_COD,particula.posicao);
				particula.mutacao = false;
			}
			
			particula.truncar();
			//Avalia a part�cula
			problema.calcularObjetivos(particula.solucao);
			//Define o melhor local
			particula.escolherLocalBest(pareto);
		}		
		
		if(rank)
			rankParticula(populacao);
		//Obt�m as melhores particulas da popula��o
		atualizarRepositorio();
		
		
		try{
			imprimirFronteira(pareto.getFronteira(),0 , "");
			//imprimirFronteira(removerGranularRaio2(pareto.getFronteira(), 0.05),0 , "OC");
			//imprimirFronteira(removerCDAS(pareto.getFronteira(), 0.45),0 , "CDAS");
		} catch (IOException ex) {ex.printStackTrace();}
		
		//System.out.println(pareto.getFronteira().size());
		removerGranularLimites(pareto.getFronteira());
		try{
			imprimirFronteira(pareto.getFronteira(),0 , "");
			//imprimirFronteira(removerGranularRaio2(pareto.getFronteira(), 0.05),0 , "OC");
			//imprimirFronteira(removerCDAS(pareto.getFronteira(), 0.45),0 , "CDAS");
		} catch (IOException ex) {ex.printStackTrace();}
	   // System.out.println(" -  " + pareto.getFronteira().size());
		
		
		
		calcularCrowdingDistance(pareto.fronteira);
		
		
		
		//System.out.println(pareto.getFronteira().size());
		
		pareto.podarLideresCrowdedOperator(tamanhoRepositorio);
				
		//Recalcula a Crowding distance dos lideres
		calcularCrowdingDistance(pareto.fronteira);
		
		/*if(rank){

			ArrayList<Particula> cloneFronteira = (ArrayList<Particula>)pareto.fronteiraNuvem.clone();

			pareto.apagarFronteiraNuvem();

			for (Iterator<Particula> iter = cloneFronteira.iterator(); iter.hasNext();) {
				Particula particula =  iter.next();
				particula.problema = this.problema;
				if(!pareto.fronteiraNuvem.contains(particula)){
					particula.solucao.numDominacao = pareto.add((Particula)particula.clone());

				}
			}	
			
			pareto.retornarFronteiraNuvem();

		}*/
	
		
		//Escolhe os novos melhores globais
		escolherLideres();
		
		escolherParticulasMutacao();
	}
	
	/**
	 * M�todo que escolhe para cada particula da populacao uma particula presente no repositorio
	 *
	 */
	public void escolherLideres(){
		for (Iterator<Solucao> iter = pareto.fronteira.iterator(); iter.hasNext();) {
			Solucao solucaotRepositorio =  iter.next();
			solucaotRepositorio.setVetorObjetivosMedio();
			solucaotRepositorio.calcularSigmaVector();
		}
		
		
		for (Iterator<Particula> iter = populacao.iterator(); iter.hasNext();) {
			Particula particula = iter.next();
			//particula.escolherGlobalOposto(pareto.fronteira);
			particula.escolherGlobalBestBinario(pareto.fronteira);
			//particula.escolherGlobalBestIdeal(pareto.fronteiraNuvem);
			//particula.escolherGlobalBestIdeal2(pareto.fronteiraNuvem);
			//particula.calcularSigmaVector();
			//particula.escolherGlobalBestSigma(pareto.fronteiraNuvem);
		}
	}
	
	/**
	 * M�todo que define quais particulas da popula��o sofrer�o mutacao
	 * AS particulas que forem domindas por mais particulas ser�o escolhidas
	 */
	public void escolherParticulasMutacao(){
		ComparetorDominacaoParticula comp = new ComparetorDominacaoParticula();
		Collections.sort(populacao, comp);
		int numMutacao = (int)(populacao.size()*INDICE_MUTACAO);
		for(int i = 1; i<=numMutacao;i++){
			Particula part = (Particula)populacao.get(populacao.size()-i);
			part.mutacao = true;
		}
	}
	

	

	public void iniciarRepositorioRank(){
		ComparetorRankParticula comp = new ComparetorRankParticula();
		Collections.sort(populacao, comp);
		for(int i = 0; i<(tamanhoRepositorio); i++){
			Particula particula = populacao.get(i);
			pareto.fronteira.add((Solucao)particula.solucao.clone());
		}
		
		
	}
	
	public void iniciarPopulacaoTeste(){
		int sl = 2;
		ArrayList<SolucaoNumerica> temp =  problema.obterSolucoesExtremas(n, sl);
		
		for (Iterator<SolucaoNumerica> iterator = temp.iterator(); iterator.hasNext();) {
			Particula particula = new Particula();
			SolucaoNumerica solucaoNumerica = (SolucaoNumerica) iterator.next();
			particula.iniciarParticulaAleatoriamente(problema, solucaoNumerica);
			problema.calcularObjetivos(solucaoNumerica);
			particula.localBestObjetivos = particula.solucao.objetivos;
			populacao.add(particula);
				
		}
		
		for(int i = 0; i<3; i++){
			Particula particula = new Particula();
			//Contador utilizada para a cria��o da regra n�o ficar presa no la�o
			int cont = 0;
			do{
				SolucaoNumerica s = new SolucaoNumerica(n, problema.m);
				s.iniciarSolucaoAleatoria();
				particula.iniciarParticulaAleatoriamente(problema, s);
				problema.calcularObjetivos(s);
				cont++;
			}while(populacao.contains(particula) && (cont<20));
			//Avaliando os objetivos da particula;
			particula.localBestObjetivos = particula.solucao.objetivos;
			populacao.add(particula);	
		}
		
		if(rank)
			rankParticula(populacao);
	}
	
	public void iniciarPopulacaoTeste2(){
		
		
		ArrayList<SolucaoNumerica> solucoes =  problema.obterFronteira(tamanhoRepositorio, 250);
		ComparetorObjetivo comp = new ComparetorObjetivo(0);
		Collections.sort(solucoes, comp);
		
		
		
		for (Iterator<SolucaoNumerica> iterator = solucoes.iterator(); iterator.hasNext();) {
			Particula particula = new Particula();
			SolucaoNumerica solucaoNumerica = (SolucaoNumerica) iterator.next();
			particula.iniciarParticulaAleatoriamente(problema, solucaoNumerica);
			problema.calcularObjetivos(solucaoNumerica);
			particula.localBestObjetivos = particula.solucao.objetivos;
			populacao.add(particula);
				
		}
		
	}
	
	public void teste(){
		
		iniciarPopulacaoTeste2();
		
		
		
		definirSExtremos(populacao);
		System.out.println();
		
		
		int k = 0;
		for (Iterator iterator = populacao.iterator(); iterator.hasNext(); k++) {
			Solucao solucao = ((Particula) iterator.next()).solucao; 
			solucao.indice = k;
		}
		
		for (Iterator iterator = populacao.iterator(); iterator.hasNext();) {
			Solucao solucao = ((Particula) iterator.next()).solucao;
			System.out.println("Solucao: " + solucao.indice);
			double dom  = pareto.add2(solucao);
			if(dom ==0)
				System.out.print("");
				
		}
		try{
			imprimirFronteira(pareto.getFronteira(), 0, "temp");
			} catch(IOException ex){ex.printStackTrace();}
	}
	

	
		
		

}
