package principal;

import indicadores.Dominance;
import indicadores.GD;
import indicadores.IGD;
import indicadores.Indicador;
import indicadores.NumeroPontos;
import indicadores.PontoFronteira;
import indicadores.PontosNaFronteira;
import indicadores.Spread;
import indicadores.Tchebycheff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;


import kernel.AlgoritmoAprendizado;
import kernel.genetic.NSGA2;
import kernel.misa.MISA;
import kernel.nuvemparticulas.SMOPSO;
import kernel.nuvemparticulas.SigmaMOPSO;

import problema.DTLZ1;
import problema.DTLZ2;
import problema.DTLZ3;
import problema.DTLZ4;
import problema.DTLZ5;
import problema.DTLZ6;
import problema.DTLZ7;
import problema.Problema;
import problema.TestCaseSelection;
import solucao.Solucao;
import solucao.SolucaoNumerica;

public class Principal {
	
	public String alg = null;
	public String prob = null;
	
	public Problema problema;
	
	public AlgoritmoAprendizado algoritmo = null;
	
	public int geracoes;
	public int populacao;
	public int numExec;
	public int m;
	public int n;
	public int k;
	
	public int repositorio;
	
	public int numeroavaliacoes=-1;
	
	
	public double S;
	public double S_MAX;
	
	public boolean rank;
	public String tipoRank;
	public String tipoPoda;
	public String escolhaLider = "";
	
	
	public String alg1;
	public String alg2;
	public String dirExec = "";
	public boolean dominance = false;
	public int num_sol_fronteira = 0;
	
	public String indicador = "";
	
	public double maioresObjetivos[];
	
	public String[] maxmimObjetivos;
	public int maxobjhiper;
	
	public int taxaclonagem;
	public int partesgrid;
	
	public String programaes; 
	public int numeroCasosTeste;
	public String funcoesobjetivo;
	
	public String tipoSolucao;
	
	public double ocupacao = 0;
	public double fator = 0;
	public double eps = 0;
	
	 
	public static void main(String[] args) {	
		Principal principal = new Principal();
		try{			
			principal.carregarArquivoConf(args[0]);
			principal.setProblema();
			if(principal.dominance){
				principal.executarDominance();
			} else {
				if(!principal.indicador.equals(""))
					principal.executarIndicador();
				else{
					if(principal.alg.equals("sigma"))
						principal.algoritmo = new SigmaMOPSO(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.maxmimObjetivos, principal.tipoRank, principal.ocupacao, principal.fator, principal.S_MAX, principal.tipoPoda, principal.eps);
					if(principal.alg.equals("smopso"))
						principal.algoritmo = new SMOPSO(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.maxmimObjetivos, principal.repositorio, principal.tipoRank, principal.ocupacao, principal.fator, principal.S_MAX, principal.tipoPoda, principal.escolhaLider,principal.eps);
					if(principal.alg.equals("misa"))
						principal.algoritmo = new MISA(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.taxaclonagem, principal.partesgrid, principal.maxmimObjetivos, principal.tipoRank, principal.ocupacao, principal.fator, principal.repositorio,principal.eps);
					if(principal.alg.equals("nsga2"))
						principal.algoritmo = new NSGA2(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.tipoSolucao, principal.maxmimObjetivos, principal.tipoRank, principal.ocupacao, principal.fator, principal.tipoPoda,principal.eps);
					
					
					principal.executar();
				
				}
			  }
			} catch (Exception ex) {ex.printStackTrace();}
	}
	
	

	private  void executar()
			throws IOException {
		System.out.println(this);
		String id = alg + "_" + prob + "_" + m;
		
		if(!rank){
			if(tipoPoda.equals(""))
				id = id + "_" + S + "_" + escolhaLider;
			else
				id = id + "_" + S + "_" + escolhaLider+ "_" + tipoPoda;
		} else{
			id = id+ "_" + S+ "_" + escolhaLider  + "_" + tipoRank;
		}
		
		String caminhoDir = null;
						
		if(!rank){
			if(tipoPoda.equals("")){
				caminhoDir = System.getProperty("user.dir") + "/resultados/" + alg + "/" +prob + "/" + m + "/" + S + "_" + escolhaLider +"/" ;
			} else{
				caminhoDir = System.getProperty("user.dir") + "/resultados/" + alg + "/" +prob + "/" + m + "/" + S + "_" + escolhaLider + "_" + tipoPoda + "/" ;
			}
		}else{
			caminhoDir = System.getProperty("user.dir") + "/resultados/" + alg + "/" +prob + "/" + m + "/" +S  + "_" + escolhaLider + "_" + tipoRank +"/" ;
		}
		
		File dir = new File(caminhoDir);
		dir.mkdirs();
		
		PrintStream psTempo =  new PrintStream(caminhoDir + id +"_texec.txt");
		PrintStream psSolucaoGeral = new PrintStream(caminhoDir +id+"_solucoes.txt");
		PrintStream psFronteiraGeral = new PrintStream(caminhoDir+id+ "_fronteira.txt");
		
		
		maioresObjetivos = new double[m];
		ArrayList<ArrayList<PontoFronteira>> fronteiras = new ArrayList<ArrayList<PontoFronteira>>();
		
		long tinicial, tfinal;
		
		
		for(int i = 0; i<numExec; i++){
			
			String caminhoDirExec = caminhoDir+i+"/";
			dir = new File(caminhoDirExec);
			dir.mkdirs();

			PrintStream psSolucaoExec = new PrintStream(caminhoDirExec+id+"_solucoes.txt");
			PrintStream psFronteiraExec = new PrintStream(caminhoDirExec+id+"_fronteira.txt");
			
			psTempo.print(i +":\t" + Calendar.getInstance().getTimeInMillis() + "\t");
			tinicial = Calendar.getInstance().getTimeInMillis();
			
			ArrayList<Solucao> solucoes = null;

			System.out.println("Execucao: " + i);
			if(numeroavaliacoes==-1)
				solucoes =  algoritmo.executar();
			else
				solucoes =  algoritmo.executarAvaliacoes();
			
			psTempo.print(Calendar.getInstance().getTimeInMillis()  + "\n");
			
			
			for (int j = 0; j < maioresObjetivos.length; j++) {
				maioresObjetivos[j] = 0;
			}
			
			ArrayList<PontoFronteira> fronteira = new ArrayList<PontoFronteira>();
			
			for (Iterator<Solucao> iterator = solucoes.iterator(); iterator.hasNext();) {
				Solucao solucao = iterator.next();
				
				PontoFronteira pf = new PontoFronteira(solucao.objetivos);
				fronteira.add(pf);
				
				for(int j = 0; j<m; j++){
					if(solucao.objetivos[j]>maioresObjetivos[j])
						maioresObjetivos[j] =Math.ceil(solucao.objetivos[j]);
				}
			}
			
			
			fronteiras.add(fronteira);
			
			gerarSaida(solucoes, psSolucaoGeral,  psFronteiraGeral, psSolucaoExec, psFronteiraExec);
			psSolucaoGeral.println();
			psFronteiraGeral.println();
		
			System.out.println();
			System.out.println("Numero de avaliacoes: " + problema.avaliacoes);
			
			System.out.println("Piores Objetivos: ");
			for (int j = 0; j < maioresObjetivos.length; j++) {
				System.out.print(maioresObjetivos[j] + "\t");
				
			}
			System.out.println();
			tfinal = Calendar.getInstance().getTimeInMillis();
			
			System.out.println("Tempo Execucao: " + ((double)(tfinal - tinicial)/1000) + " (s)");
			System.out.println();
		}
		
		
		
			
		Spread spread = new Spread(m, caminhoDir, id);
		spread.preencherObjetivosMaxMin(maxmimObjetivos);
		spread.calcularIndicadorArray(fronteiras);
		
		ArrayList<PontoFronteira> pftrue= carregarFronteiraPareto(System.getProperty("user.dir"), prob, m);

		GD gd = new GD(m, caminhoDir, id, pftrue);
		gd.preencherObjetivosMaxMin(maxmimObjetivos);
		gd.calcularIndicadorArray(fronteiras);

		IGD igd = new IGD(m, caminhoDir, id, pftrue);
		igd.preencherObjetivosMaxMin(maxmimObjetivos);
		igd.calcularIndicadorArray(fronteiras);

		double[] j =  problema.getJoelho(n, pftrue);
		double[] l = problema.getLambda(n, pftrue);
		Tchebycheff tcheb = new Tchebycheff(m, caminhoDir, id, j , l);
		tcheb.preencherObjetivosMaxMin(maxmimObjetivos);
		tcheb.calcularTchebycheff(fronteiras);

		/*PontosNaFronteira pnf = new PontosNaFronteira(m, caminhoDir, idInd, pftrue);
			pnf.preencherObjetivosMaxMin(maxmimObjetivos);
			pnf.calcularIndicadorArray(fronteiras);*/

		NumeroPontos np = new NumeroPontos(m, caminhoDir, id);
		np.preencherObjetivosMaxMin(maxmimObjetivos);
		np.calcularIndicadorArray(fronteiras);

	}
	
	public void gerarSaida(ArrayList<Solucao> fronteira, PrintStream solGeral, PrintStream psFronteiraGeral, PrintStream solExecucao, PrintStream psFronteiraExec){
		
		for (Iterator<Solucao> iterator = fronteira.iterator(); iterator.hasNext();) {
			Solucao solucao = iterator.next();
			solGeral.println(solucao);
			solExecucao.println(solucao);
			for(int i = 0; i<m; i++){
				//psFronteiraExec.print(new Double( solucao.objetivos[i])+ "\t");
				//psFronteiraGeral.print(new Double(solucao.objetivos[i]) + "\t");
				psFronteiraExec.print(new Double( solucao.objetivos[i]).toString().replace('.', ',')+ "\t");
				psFronteiraGeral.print(new Double(solucao.objetivos[i]).toString().replace('.', ',') + "\t");	
			}
			psFronteiraExec.println();
			psFronteiraGeral.println();
		}
	}
	
	private void executarDominance() throws IOException{
		
		if(alg1 == null || alg2 == null){
			System.err.println("Algoritmos para a compara��o da dominancia n�o foram definido (Tags alg1 ou alg2)");
			System.exit(0);
		}
			
		
		String caminhoDir = System.getProperty("user.dir") + "/resultados/" + alg + "/" +prob + "/" + m + "/";
		File dir = new File(caminhoDir);
		dir.mkdirs();
		
		String idExec1 = alg + prob + "_" + m + alg1;
		String idExec2 = alg + prob + "_" + m + alg2;
		
		Dominance dominance = new Dominance(m, caminhoDir, idExec1, idExec2);
		dominance.preencherObjetivosMaxMin(maxmimObjetivos);
		String arquivo1 = caminhoDir + alg1 + "/" + idExec1  + "_fronteira.txt";
		String arquivo2 = caminhoDir + alg2 + "/" + idExec2  + "_fronteira.txt";
		
		dominance.calcularDominanceArquivo(arquivo1, arquivo2);
		
	}
	
	private void executarIndicador() throws IOException{
		
		if(alg1 == null){
			System.err.println("Algoritmo para a execucao do indicador n�o foi definido (Tags alg1)");
			System.exit(0);
		}
		
		
		String[] configuracoes = {"0.25", "0.3", "0.35", "0.4", "0.45", "0.5", "0.55", "0.6", "0.65", "0.7", "0.75"};
		if(!alg1.equals("all")){
			configuracoes = new String[1];
			configuracoes[0] = alg1;
		}
		
		ArrayList<SolucaoNumerica> fronteira = null;
		ArrayList<PontoFronteira> pftrue= null;
		double[] j = null;
		double[] l = null;
		if(indicador.equals("gd") || indicador.equals("igd") || indicador.equals("tcheb") || indicador.equals("pnf")){
			System.out.println("Obtendo Fronteira: " + problema.problema + " " + m);
			if(num_sol_fronteira == 0)
				num_sol_fronteira = populacao;
			fronteira =  problema.obterFronteira(n, num_sol_fronteira);
			pftrue= new ArrayList<PontoFronteira>();

			for (Iterator<SolucaoNumerica> iterator = fronteira.iterator(); iterator.hasNext();) {
				SolucaoNumerica solucao = (SolucaoNumerica) iterator.next();
				PontoFronteira temp = new PontoFronteira(solucao.objetivos);
				pftrue.add(temp);
			}
			
			j =  problema.getJoelho(n, pftrue);
			l = problema.getLambda(n, pftrue);

		}
		
		for (int i = 0; i < configuracoes.length; i++) {
			alg1 = configuracoes[i];
			String diretorio = dirExec;
			if(diretorio.equals(""))
				diretorio = System.getProperty("user.dir");

			String caminhoDir = diretorio + "resultados/" + alg + "/" +prob + "/" + m + "/" + alg1 +"/";
			File dir = new File(caminhoDir);
			dir.mkdirs();

			String idExec = alg + prob + "_" + m + alg1;
			Indicador ind = null;
			if(indicador.equals("gd")){
					ind = new GD(m, caminhoDir, idExec, pftrue);
			}
			if(indicador.equals("igd")){
						ind = new IGD(m, caminhoDir, idExec, pftrue);
			}

			if(indicador.equals("tcheb"))
				ind = new Tchebycheff(m, caminhoDir, idExec, j , l);
				
		
			if(indicador.equals("spread"))
					ind = new Spread(m, caminhoDir, idExec);
			
			
			if(indicador.equals("pnf"))
				ind = new PontosNaFronteira(m, caminhoDir, idExec, pftrue);
				
			if(indicador.equals("np"))
				ind = new NumeroPontos(m, caminhoDir, idExec);
			
			if(ind!=null){
				ind.preencherObjetivosMaxMin(maxmimObjetivos);
				String arquivo1 = caminhoDir + "/" + idExec  + "_fronteira.txt";
				System.out.println("Indicador: " + ind.indicador);
				System.out.println("S = " + alg1);
				ind.calcularIndicadorArquivo(arquivo1);
			}
		}
	}
	
	public ArrayList<PontoFronteira> carregarFronteiraPareto(String dir, String problema, int objetivo){
		ArrayList<PontoFronteira> pftrue = new ArrayList<PontoFronteira>();
		try{
			String arquivo = dir + "/pareto/" + problema + "_" + objetivo + "_pareto.txt";
			BufferedReader buff = new BufferedReader(new FileReader(arquivo));
			while(buff.ready()){
				String linha = buff.readLine().trim();
				if(!linha.equals("")){
					String[] linha_split = linha.split("\t");
					double[] valores = new double[objetivo]; 
					for (int i = 0; i < linha_split.length; i++) {
						valores[i] = new Double(linha_split[i]);
					}
					PontoFronteira pf = new PontoFronteira(valores);
					pftrue.add(pf);
				}
			}
		}
		catch(IOException ex){ex.printStackTrace();}
		
		return pftrue;
		
	}
	
	
	
	public void setProblema(){
		prob = prob.toUpperCase();
		if(prob.equals("TESTSELECTION")){
			problema = new TestCaseSelection(m, funcoesobjetivo, programaes, dirExec, numeroCasosTeste);
			tipoSolucao = "binaria";
			n = numeroCasosTeste;
		} else{
		tipoSolucao = "numerica";
		if(prob.equals("DTLZ1"))
			problema = new DTLZ1(m);
		if(prob.equals("DTLZ2"))
			problema = new DTLZ2(m);
		if(prob.equals("DTLZ3"))
			problema = new DTLZ3(m);
		if(prob.equals("DTLZ4"))
			problema = new DTLZ4(m);
		if(prob.equals("DTLZ5"))
			problema = new DTLZ5(m);
		if(prob.equals("DTLZ6"))
			problema = new DTLZ6(m);
		if(prob.equals("DTLZ7"))
			problema = new DTLZ7(m);
		}
	}
	
	public void carregarArquivoConf(String nomeArquivo)throws IOException{
		Reader reader = new FileReader(nomeArquivo);
		BufferedReader buff = new BufferedReader(reader);
		while(buff.ready()){
			String linhaString = buff.readLine();
			if(!linhaString.isEmpty()){
				String linha[] = linhaString.split("=");

				if(linha.length!=2){
					System.err.println("Erro no arquivo de configura��o. Linha: " + linhaString);
					System.exit(0);
				}
				String tag = linha[0].trim().toLowerCase();
				String valor = linha[1].trim();
				if(tag.equals("algoritmo"))
					alg = valor;

				if(tag.equals("direxec"))
					dirExec = valor;

				if(tag.equals("programaes"))
					programaes = valor;

				if(tag.equals("funcoesobjetivo"))
					funcoesobjetivo = valor;

				if(tag.equals("numerocasosteste"))
					numeroCasosTeste = new Integer(valor).intValue();

				if(tag.equals("geracoes")){
					geracoes = new Integer(valor).intValue();
				}
				if(tag.equals("populacao"))
					populacao = new Integer(valor).intValue();
				if(tag.equals("repositorio"))
					repositorio = new Integer(valor).intValue();
				if(tag.equals("numexec"))
					numExec = new Integer(valor).intValue();
				if(tag.equals("m"))
					m = new Integer(valor).intValue();
				if(tag.equals("k"))
					k = new Integer(valor).intValue();
				if(tag.equals("ocupacao"))
					ocupacao = new Double(valor).doubleValue();
				if(tag.equals("fator"))
					fator = new Double(valor).doubleValue();
				
				if(tag.equals("eps"))
					eps = new Double(valor).doubleValue();

				if(tag.equals("num_sol_fronteira"))
					num_sol_fronteira = new Integer(valor).intValue();


				if(tag.equals("taxaclonagem"))
					taxaclonagem = new Integer(valor).intValue();

				if(tag.equals("maxobjhiper"))
					maxobjhiper = new Integer(valor).intValue();

				if(tag.equals("numeroavaliacoes"))
					numeroavaliacoes = new Integer(valor).intValue();


				if(tag.equals("problema")){
					prob = valor;
				}

				if(tag.equals("dominance")){
					if(valor.equals("true"))
						dominance = true;
					else
						dominance = false;
				}

				if(tag.equals("indicador"))
					if(!valor.equals("false")){
						indicador = valor;
					}

				if(tag.equals("alg1")){
					alg1 = valor;
				}

				if(tag.equals("alg2")){
					alg2 = valor;
				}

				if(tag.equals("s"))
					S = new Double(valor).doubleValue();
				if(tag.equals("s_max"))
					S_MAX = new Double(valor).doubleValue();
				if(tag.equals("partesgrid")){
					partesgrid = new Integer(valor).intValue();
				}

				
				if(tag.equals("max_min")){
					StringBuffer obj = new StringBuffer();
					for(int i = 0;i<m; i++)
						obj.append(" " + valor);
					maxmimObjetivos = obj.toString().trim().split(" ");
				}

				if(tag.equals("rank")){
					if(valor.equals("false")){
						rank = false;
						tipoRank = "";
					}
					else{
						rank = true;
						tipoRank = valor;
					}
				}
				
				if(tag.equals("poda")){
					if(valor.equals("false")){
						tipoPoda = "";
					}
					else{
						tipoPoda = valor;
					}
				}
				
				if(tag.equals("lider")){					
					escolhaLider = valor;
				}
			}
		}
		
		n = m + k - 1;
		
		
	}
	
	
	
	
	public String toString(){
		StringBuffer buff = new StringBuffer();
		buff.append("Algoritmo: " + alg + "\n");
		buff.append("Problema: " + prob + "\n");
		buff.append("m: " + m + "\n");
		buff.append("n: " + n + "\n");
		buff.append("Geracoes: " + geracoes + "\n");
		buff.append("Avaliacoes: " + numeroavaliacoes + "\n");
		buff.append("Populacao: " + populacao + "\n");
		buff.append("S: " + S + "\n");
		buff.append("poda: " + tipoPoda + "\n");
		buff.append("lider: " + escolhaLider + "\n");
		return buff.toString();
	}

}