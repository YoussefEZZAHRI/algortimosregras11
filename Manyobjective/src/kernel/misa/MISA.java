package kernel.misa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import pareto.FronteiraPareto;
import problema.DTLZ2;
import problema.Problema;

import solucao.ComparetorDominacao;
import solucao.ComparetorRank;
import solucao.SolucaoNumerica;
import kernel.AlgoritmoAprendizado;


/**
 * Algoritmo MISA proposto por Coello em: Solving Multiobjective Optimization Problems Using
an Artificial Immune System
 * @author Andre
 *
 */
public class MISA extends AlgoritmoAprendizado {

	
	public ArrayList<SolucaoNumerica> populacao = null;
	public AdaptiveGrid populacaoSecundaria = null;
	public ArrayList<SolucaoNumerica> clones = null;
	
	//Total de elementos ap�s a clonagem
	public int totalClonagem;
	
	public double s;
	
	//Taxa de clonagem
	public int taxaClonagem = 1;
	//Numero de divisoes do grid da populacaos secundaria
	public final int partesGrid;
	//Probabilidade da mutacao nao uniforme
	public double non_uniform_prob;
	public double decremento;
	
	/**
	 * 
	 * @param n N�mero de variaveis
	 * @param prob Problema
	 * @param g N�mero de gera��es
	 * @param t Tamanho m�ximo da popula��o
	 * @param s Valor do S do m�todo do Sato
	 * @param tc Taxa de clonagem
	 * * @param pg N�mero de partes da divis�o do grid da popula��o secund�ria
	 */
	public MISA(int n, Problema prob, int g, int a, int t, double s, int tc, int pg){
		super(n,prob,g,a,t);
		
		taxaClonagem = tc;
		totalClonagem = taxaClonagem * tamanhoPopulacao;
		partesGrid = pg;
		
		this.s = s;
		
		//Probabilidade inicial da mutacao n�o uniforme
		non_uniform_prob = 0.6;
		//Decremento da muta��o n�o uniforme, variando do incial at� 1/n
		double diff = non_uniform_prob - PROB_MUT_COD;
		decremento = diff/(double) t;
	}
	
	@Override
	
	public ArrayList<SolucaoNumerica> executar() {
		
		//Inicio aleat�rio da popula��o
		iniciarPopulacao();
		
		problema.avaliacoes = 0;
		
		
		//La�o evolutivo
		for(int g = 0; g<geracoes; g++){
			if(g%10 == 0)
				System.out.print(g + " ");
			//Sele��o das melhores solu��es
			lacoEvolutivo();	
		}
		return populacao;
	}
	
	public ArrayList<SolucaoNumerica> executarAvaliacoes() {
		
		//Inicio aleat�rio da popula��o
		iniciarPopulacao();
		//System.out.println(this.toString());
		problema.avaliacoes = 0;
		
		
		//La�o evolutivo
		while(problema.avaliacoes<numeroavalicoes){
			lacoEvolutivo();
			if(problema.avaliacoes % 10 == 0)
				System.out.print(problema.avaliacoes + " - ");
		}
		System.out.println();
		
		
	
		return populacao;
	}

	private void lacoEvolutivo() {
		encontrarSolucoesNaoDominadas(populacao, pareto);
		ArrayList<SolucaoNumerica> melhores = obterMelhoresAnticorpos(pareto, populacao, 0.05);
		//Adiciona as melhores solu��es do problema no grid da populacao secundaria
		for (Iterator<SolucaoNumerica> iterator = melhores.iterator(); iterator.hasNext();) {
			SolucaoNumerica solucao = (SolucaoNumerica) iterator.next();
			solucao.aceita = populacaoSecundaria.add(solucao);
		}
		//Obt�m as solu��es presentes no grid
		clones = populacaoSecundaria.getAll();
		
		//Colna os melhores anticorpos
		clonarMelhoresAnticorpos(clones);	
		//Aplica uma muta��o em todos os clones
		mutacao(clones, PROB_MUT_COD);
		//Aplica uma muta��o n�o uniform nos clones dominados
		//mutacaoSolucoesNaoTaoBoas(clones);			
		//Adiciona todos os clones na popula��o atual
		populacao.addAll(clones);
		//Obt�m os novos l�deres da popula��o
		FronteiraPareto paretoTemp = new FronteiraPareto(pareto.S);
		encontrarSolucoesNaoDominadas(populacao, paretoTemp);
		//Reduz a popula��o com o tamanho passado como parametro
		reduzirPopulacao(populacao, paretoTemp);
	}
	
	/*for (Iterator iterator = clones.iterator(); iterator.hasNext();) {
		Solucao solucao = (Solucao) iterator.next();
		for (int i = 0; i < solucao.variaveis.length; i++) {
			if(solucao.variaveis[i]<0)
				System.out.println();
		}	
	}*/
	
	/**
	 * M�todo que inicia a popula��o aleatoriamente e inicia a popula��o secund�ria como vazia
	 */
	public void iniciarPopulacao(){
		populacao = new ArrayList<SolucaoNumerica>();
		for(int i = 0; i<tamanhoPopulacao; i++){
			SolucaoNumerica s = new SolucaoNumerica(n, problema.m);
			s.iniciarSolucaoAleatoria();
			populacao.add(s);
			problema.calcularObjetivos(s);
		}
		
		populacaoSecundaria = new AdaptiveGrid(problema.m, partesGrid);
		clones = null;
		pareto = new FronteiraPareto(s);
	}
	

	/**
	 * M�todo que reduz a popula��o para o tamanho m�ximo passado como parametro
	 * @param populacaoFinal Popula��o final da itera��o
	 * @param paretoTemp Conjunto das melhore solu��s encontradas at� ent�o
	 */
	public void reduzirPopulacao(ArrayList<SolucaoNumerica> populacaoFinal, FronteiraPareto paretoTemp){
		//Se o n�mero das melhores solu��es � menor que o tamanho m�ximo, todas as solu��es s�o adicionadas na popula��o final 
		//e as melhores solu��es (domindas por menos solu��es) dominadas da popula��o
		//Caso contr�rio somente as melhore solu��es s�o adicionadas na popula��o final
		if(paretoTemp.fronteira.size()<tamanhoPopulacao){
			ArrayList<SolucaoNumerica> temp = obterMelhoresAnticorpos(paretoTemp, populacaoFinal, 1.0);
			populacaoFinal.clear();
			populacaoFinal.addAll(temp);
		}
		else{
			ArrayList<SolucaoNumerica> solucoesFinais = paretoTemp.fronteira;
			//Escolhe as melhores solu��es atraves do metodo AR 
			averageRank(solucoesFinais);
			ComparetorRank comp = new ComparetorRank();
			Collections.sort(solucoesFinais, comp);
			
			
			populacaoFinal.clear();
			for(int i = 0; i<tamanhoPopulacao; i++)
				populacaoFinal.add(solucoesFinais.get(i));	
		}
	}
	
	/**
	 * M�todo que obt�m as melhores solu��es. Caso o n�mero de solu��es seja menor que o valor porcentagemaMinima passado como parametro,
	 * as melhores dominadas solu��es s�o adicionadas nas melhores.
	 * @param paretoAtual Solu�oes n�o dominadas
	 * @param porcentagemaMinima Porcentagem m�nima de solu��es que devem ser retornadas
	 * @return
	 */
	public ArrayList<SolucaoNumerica> obterMelhoresAnticorpos(FronteiraPareto paretoAtual, ArrayList<SolucaoNumerica> populacao,  double porcentagemaMinima){
		ArrayList<SolucaoNumerica> melhores = new ArrayList<SolucaoNumerica>();
		melhores.addAll(paretoAtual.fronteira);
		int maxMelhores = (int)(porcentagemaMinima*tamanhoPopulacao);
		//Caso o n�mero das melhores solu��es seja menor que a porcentamge tamanhoMelhores da popula��o deve-se preencher os array das melhores solu��es
		if(melhores.size()< maxMelhores){
			ArrayList<SolucaoNumerica> dominadas = new ArrayList<SolucaoNumerica>();
			for (Iterator<SolucaoNumerica> iterator = populacao.iterator(); iterator.hasNext();) {
				SolucaoNumerica solucao = (SolucaoNumerica) iterator.next();
				if(!melhores.contains(solucao)){
					dominadas.add(solucao);
				}
			}
			
			//Ordena as solu��es de acordo com o n�mero de dominacao de cada solucao
			ComparetorDominacao comp = new ComparetorDominacao();
			Collections.sort(dominadas, comp);
			int resto = maxMelhores - melhores.size();
			for(int i = 0; i<resto; i++)
				melhores.add((SolucaoNumerica) dominadas.get(i));	
		}
		return melhores;
	}
	
	/**
	 * Clona as melhores solu��es da itera��o
	 * @param clones Melhores solucoes da itera��o
	 */
	public void clonarMelhoresAnticorpos(ArrayList<SolucaoNumerica> clones){
		//Calcula o numero estimado de clones para cada elemento a ser colnado
		int numEstimadoClones = (taxaClonagem*populacao.size())/populacaoSecundaria.size();
		clones = new ArrayList<SolucaoNumerica>();
		if(populacaoSecundaria.isFull())
			clonarFull(numEstimadoClones, populacaoSecundaria.getAll());
		else
			clonarNotFull(numEstimadoClones, populacaoSecundaria.getAll());
		
	}
	
	/**
	 * Clona as melhores solu��es caso a popula��o secund�ria esteja cheia
	 * @param numBase N�mero estimado de clones para cada solu��o
	 * @param melhores Solu��es que ser�o clonadas
	 */
	public void clonarFull(int numBase, ArrayList<SolucaoNumerica> melhores){
		for (Iterator<SolucaoNumerica> iterator = melhores.iterator(); iterator.hasNext();) {
			SolucaoNumerica solucao = (SolucaoNumerica) iterator.next();
			double fator = 1;
			if(!solucao.aceita)
				fator = 0;
			else{
				//Calcula quanto cada elemento vai ser clonado de acordo com a ocupa��o da c�lula em que o elemento pertence no grid
				double mediaOcupacao = populacaoSecundaria.obterMediaOcupacao();
				Integer celula = populacaoSecundaria.contains(solucao);				
				int crowdCelula = populacaoSecundaria.obterLotacao(celula, solucao);
				if(crowdCelula<mediaOcupacao)
					fator = 2;
				else
					fator = 0.5;
			}
			for(int j = 0; j<numBase*fator; j++){
				SolucaoNumerica novaSolucao = (SolucaoNumerica)solucao.clone();
				clones.add(novaSolucao);
			}
			
		}
		
	}
	
	/**
	 * Clona as melhores solu��es caso a popula��o secund�ria n�o esteja cheia
	 * @param numBase N�mero estimado de clones para cada solu��o
	 * @param melhores Solu��es que ser�o clonadas
	 */
	public void clonarNotFull(int numBase, ArrayList<SolucaoNumerica> melhores){
		double[][] distancias = new double[melhores.size()][melhores.size()];
		double[] mediasIndividuais = new double[melhores.size()];
		double mediaDistancias = 0;
		double numDist = 0;
		for(int i = 0; i< melhores.size()-1;i++){
			SolucaoNumerica solucao1 = melhores.get(i);
			for(int j = i+1; j<melhores.size(); j++){
				SolucaoNumerica solucao2 = melhores.get(j);
				distancias[i][j] = distanciaEuclidiana(solucao1.objetivos, solucao2.objetivos);
				mediaDistancias+= distancias[i][j];
				mediasIndividuais[i] += distancias[i][j];
				mediasIndividuais[j] += distancias[i][j];
				numDist++;
			}
		}
		
		mediaDistancias = mediaDistancias/numDist;
		for (int i = 0; i < mediasIndividuais.length; i++) {
			mediasIndividuais[i] = mediasIndividuais[i]/(mediasIndividuais.length-1);
		}
		
		int densidadeAbaixo = 0;
		int densidadeAcima = 0;
		
		for(int i = 0; i< mediasIndividuais.length;i++){
			if(mediasIndividuais[i]<mediaDistancias)
				densidadeAbaixo++;
			else
				densidadeAcima++;
		}
		
		int i = 0;
		for (Iterator<SolucaoNumerica> iterator = melhores.iterator(); iterator.hasNext();) {
			SolucaoNumerica solucao = (SolucaoNumerica) iterator.next();
			double fator = 1;
			//Se a solu��o pertence � parte de baixo
			if(mediasIndividuais[i]<mediaDistancias){
				//Se a parte de baixo � mais densa ent�o reduz o n�mero de clones em 50%
				if(densidadeAbaixo>=densidadeAcima)
					fator = 0.5;
				else
					//Caso contr�rio aumenta o n�mero em 50%
					fator = 1.5;
			} else {
				//Se a solu��o pertence � parte de cima
				if(mediasIndividuais[i]>mediaDistancias)
					//Se a parte de cima � mais densa reduz em 50%, caso contr�rio aumenta em 50%
					if(densidadeAbaixo<densidadeAcima)
						fator = 0.5;
					else
						fator = 1.5;
			}
			i++;
			for(int j = 0; j<numBase*fator; j++){
				SolucaoNumerica novaSolucao = (SolucaoNumerica)solucao.clone();
				clones.add(novaSolucao);
			}	
		}
	}
	
	/**
	 * Aplica uma muta��o polinomial com probabilidade prob em todos os clones
	 * @param solucoes
	 */
	public void mutacao(ArrayList<SolucaoNumerica> solucoes, double prob){
		for (Iterator<SolucaoNumerica> iterator = solucoes.iterator(); iterator.hasNext();) {
			SolucaoNumerica solucao = (SolucaoNumerica) iterator.next();

			mutacaoPolinomial(prob, solucao);
			
			solucao.truncar();
			
			problema.calcularObjetivos(solucao);		

		}
	}
	
	public void mutacaoSolucoesNaoTaoBoas(ArrayList<SolucaoNumerica> clones){
		
		FronteiraPareto clonesNaoDominados = new FronteiraPareto(pareto.S);
		encontrarSolucoesNaoDominadas(clones, clonesNaoDominados);
		ArrayList<SolucaoNumerica> melhores = clonesNaoDominados.getFronteira();
		
		ArrayList<SolucaoNumerica> dominadas = new ArrayList<SolucaoNumerica>();
		for (Iterator<SolucaoNumerica> iterator = clones.iterator(); iterator.hasNext();) {
			SolucaoNumerica solucao = (SolucaoNumerica) iterator.next();
			if(!melhores.contains(solucao)){
				dominadas.add(solucao);
			}
		}
		
		mutacao(dominadas, non_uniform_prob);
		non_uniform_prob -= decremento;
		
	}
	
	public String toString(){
		StringBuffer buff = new StringBuffer();
		int tamPop = 0;
		int tamClone = 0;
		int tamSec = 0;
		
		
		if(populacao !=null)
			tamPop = populacao.size();
		if(clones !=null)
			tamClone = clones.size();
		if(populacaoSecundaria!=null)	
			tamSec = populacaoSecundaria.size();
			
		buff.append("Populacao: " +tamPop + "\n");
		buff.append("Clones: " +tamClone + "\n");
		buff.append("Secundaria: " + tamSec + "\n");
		buff.append("Pareto: " + pareto.fronteira.size());
		return buff.toString();
	}
	

	
	public static void main(String[] args) {
		int n = 10;
		int m = 3;
		Problema prob = new DTLZ2(m);
		
		int g = 50;
		int t = 100;
		
		int a = -1;
		
		MISA misa = new MISA(n, prob, g, a,t, 0.25, 7, 25);
		
		misa.executar();
		
		
	}
	

}