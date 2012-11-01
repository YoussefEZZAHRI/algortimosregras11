package principal;

import indicadores.Convergence;
import indicadores.Dominance;
import indicadores.Evaluations;
import indicadores.GD;
import indicadores.HistogramDTLZ2;
import indicadores.IGD;
import indicadores.Indicador;
import indicadores.LargestDistance;
import indicadores.NumeroPontos;
import indicadores.PontoFronteira;
import indicadores.PontosNaFronteira;
import indicadores.Spacing;
import indicadores.Tchebycheff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import archive.HyperPlaneReferenceArchive;


import kernel.AlgoritmoAprendizado;
import kernel.genetic.NSGA2;
import kernel.misa.MISA;
import kernel.mopso.SMPSO;
import kernel.mopso.SigmaMOPSO;
import kernel.mopso.multi.IteratedMultiSwarm;
import kernel.mopso.multi.MultiSwarm;
import kernel.mopso.multi.TreeMultiSwarm;

import problema.DTLZ1;
import problema.DTLZ2;
import problema.DTLZ3;
import problema.DTLZ4;
import problema.DTLZ5;
import problema.DTLZ6;
import problema.DTLZ7;
import problema.Problema;
import problema.TestCaseSelection;
import problema.ZDT1;
import problema.ZDT2;
import problema.ZDT3;
import solucao.Solucao;


public class Principal {
	
	public String alg = null;
	public String prob = "";
	
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
	
	
	public String S;
	public double S_MAX;
	
	public boolean rank;
	public String tipoRank;
	public String tipoArquivo;
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
	
	public int swarms = 1;
	public boolean shared = false;
	
	public int pop_swarm = 10;
	public int rep_swarm = 200;
	public int split_iterations = 10;
	
	public int update = 1;
	
	public String front = "";
	public String true_pareto = ""; 
	
	public double box_range_beg = 0.5;
	public double box_range_end = 0.1;
	
	public boolean eval_analysis = false;
	public boolean reset = false;
	public String initialize = "ext";
	
	public Principal() {
		n = -1;
	}
	
	 
	public static void main(String[] args) {	
		Principal principal = new Principal();
		try{			
			principal.carregarArquivoConf(args[0]);
			principal.setProblema();
			System.out.println(principal);
			if(principal.dominance){
				principal.executarDominance();
			} else {
				if(!principal.indicador.equals(""))
					principal.executarIndicador();
				else{
					if(principal.alg.equals("sigma"))
						principal.algoritmo = new SigmaMOPSO(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.maxmimObjetivos, principal.tipoRank, principal.S_MAX, principal.tipoArquivo, principal.eps, principal.eval_analysis);
					if(principal.alg.equals("smopso"))
						principal.algoritmo = new SMPSO(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.maxmimObjetivos, principal.repositorio, principal.tipoRank, principal.S_MAX, principal.tipoArquivo, principal.escolhaLider,principal.eps, principal.eval_analysis);
					if(principal.alg.equals("misa"))
						principal.algoritmo = new MISA(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.taxaclonagem, principal.partesgrid, principal.maxmimObjetivos, principal.tipoRank, principal.repositorio,principal.eps, principal.tipoArquivo, principal.eval_analysis);
					if(principal.alg.equals("nsga2"))
						principal.algoritmo = new NSGA2(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.tipoSolucao, principal.maxmimObjetivos, principal.tipoRank, principal.tipoArquivo,principal.eps, principal.repositorio, principal.eval_analysis);
					if(principal.alg.equals("multi"))
						principal.algoritmo = new MultiSwarm(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.maxmimObjetivos, principal.repositorio , principal.tipoRank, principal.tipoArquivo, principal.escolhaLider,principal.eps, principal.swarms, principal.shared, principal.update, principal.eval_analysis);
					if(principal.alg.equals("imulti"))
						principal.algoritmo = new IteratedMultiSwarm(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.maxmimObjetivos, principal.repositorio , principal.tipoRank, principal.tipoArquivo, principal.escolhaLider,principal.eps, principal.swarms, principal.box_range_beg, principal.box_range_end, principal.pop_swarm, principal.rep_swarm, principal.split_iterations, principal.eval_analysis, principal.reset, principal.initialize);

					if(principal.alg.equals("tmulti"))
						principal.algoritmo = new  TreeMultiSwarm(principal.n, principal.problema, principal.geracoes, principal.numeroavaliacoes, principal.populacao, principal.S, principal.maxmimObjetivos, principal.repositorio , principal.tipoRank, principal.tipoArquivo, principal.escolhaLider,principal.eps, principal.swarms, principal.update, principal.eval_analysis);

					principal.executar(args[0]);
				
				}
			  }
			} catch (Exception ex) {ex.printStackTrace();}
	}
	
	

	private  void executar(String parameters)
			throws IOException {
		
		

		String pre_id = alg + "_" + prob + "_" + m;
		
		if(tipoArquivo.equals("eapp") || tipoArquivo.equals("eaps")){
			S = eps+"";
		}
		
		if(tipoArquivo.equals("hyper")){
			HyperPlaneReferenceArchive hyper_archiver = (HyperPlaneReferenceArchive) algoritmo.archiver;
			S = hyper_archiver.reference_index+"";
			
		}
		
		String pos_id = null;
		
		if(!rank){
			pos_id = S + "_" + escolhaLider+ "_" + tipoArquivo;
		} else{
			pos_id = S+ "_" + escolhaLider  + "_" + tipoRank;
		}
		
		if(alg.equals("imulti")){
			String temp[] = escolhaLider.split(";");
			escolhaLider = temp[0];
			tipoArquivo = tipoArquivo.split(";")[0];
			
			if(reset)
				pos_id = escolhaLider+ "_" + tipoArquivo + "_" + swarms + "_" + initialize + "_r";
			else
				pos_id = escolhaLider+ "_" + tipoArquivo + "_" + swarms + "_" + initialize;
		}
		
		if(alg.equals("multi")){
			tipoArquivo =  tipoArquivo.replace(';', '_');
			escolhaLider = escolhaLider.split(";")[0];
			pos_id = update + "_" + escolhaLider+ "_" + tipoArquivo + "_" + swarms + "_" + populacao;
			
		}
		
		String caminhoDir = System.getProperty("user.dir") + "/resultados/" + alg + "/" +prob + "/" + m + "/" + pos_id +"/" ;
		
		File dir = new File(caminhoDir);
		dir.mkdirs();
		
		String id = pre_id + "_" + pos_id;
		
		PrintStream psTempo =  new PrintStream(caminhoDir + id +"_texec.txt");
		PrintStream psSolucaoGeral = new PrintStream(caminhoDir +id+"_solucoes.txt");
		PrintStream psFronteiraGeral = new PrintStream(caminhoDir+id+ "_fronteira.txt");
		
		PrintStream psParameter =  new PrintStream(caminhoDir + id +"_parameters.txt");
		
		printParameters(psParameter, parameters);
		
		
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
		
		
		
			
		Spacing spread = new Spacing(m, caminhoDir, id);
		spread.preencherObjetivosMaxMin(maxmimObjetivos);
		spread.calcularIndicadorArray(fronteiras);
		
		ArrayList<PontoFronteira> pftrue= carregarFronteiraPareto(System.getProperty("user.dir"), prob, m);

		GD gd = new GD(m, caminhoDir, id, pftrue);
		gd.preencherObjetivosMaxMin(maxmimObjetivos);
		gd.calcularIndicadorArray(fronteiras);

		IGD igd = new IGD(m, caminhoDir, id, pftrue);
		igd.preencherObjetivosMaxMin(maxmimObjetivos);
		igd.calcularIndicadorArray(fronteiras);
		
		LargestDistance ld = new LargestDistance(m, caminhoDir, id, pftrue);
		ld.preencherObjetivosMaxMin(maxmimObjetivos);
		ld.calcularIndicadorArray(fronteiras);
		
		Convergence con = new Convergence(m, caminhoDir, id, pftrue);
		con.preencherObjetivosMaxMin(maxmimObjetivos);
		con.calcularIndicadorArray(fronteiras);

		double[] j =  problema.getJoelho(n, pftrue);
		double[] l = problema.getLambda(n, pftrue);
		
		Tchebycheff tcheb = new Tchebycheff(m, caminhoDir, id, j , l);
		tcheb.preencherObjetivosMaxMin(maxmimObjetivos);
		tcheb.calcularTchebycheff(fronteiras);
		
		if(prob.toUpperCase().equals("DTLZ2")){

			HistogramDTLZ2 hist = new HistogramDTLZ2(m, caminhoDir, id);
			hist.preencherObjetivosMaxMin(maxmimObjetivos);
			hist.generateHistogram(fronteiras);
		}

		/*PontosNaFronteira pnf = new PontosNaFronteira(m, caminhoDir, idInd, pftrue);
			pnf.preencherObjetivosMaxMin(maxmimObjetivos);
			pnf.calcularIndicadorArray(fronteiras);*/

		NumeroPontos np = new NumeroPontos(m, caminhoDir, id);
		np.preencherObjetivosMaxMin(maxmimObjetivos);
		np.calcularIndicadorArray(fronteiras);
		
		Evaluations eval = new Evaluations(m, caminhoDir, id, problema);
		eval.calcularIndicadorArray(fronteiras);
		
		if(tipoArquivo.equals("hyper")){
			PrintStream psSolucaoHyper = new PrintStream(caminhoDir+id+"_reference.txt");
			HyperPlaneReferenceArchive hyper_archiver = (HyperPlaneReferenceArchive) algoritmo.archiver;
			for(int k = 0; k<m; k++){
				psSolucaoHyper.print(hyper_archiver.reference_point[k] + "\t");
			}
			psSolucaoHyper.println();
			
		}

	}


	private void printParameters(PrintStream psParameters, String fileParameters) throws FileNotFoundException, IOException {
		Reader reader = new FileReader(fileParameters);
		BufferedReader buff = new BufferedReader(reader);
		while(buff.ready()){
			String linhaString = buff.readLine();
			if(!linhaString.isEmpty()){
				psParameters.println(linhaString);
			}
		}
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
			System.err.println("Algoritmos para a comparacaoo da dominancia nao foram definido (Tags alg1 ou alg2)");
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
	
	public void executarIndicador() throws IOException{


		ArrayList<PontoFronteira> pftrue= null;
		double[] j = null;
		double[] l = null;
		if(indicador.equals("gd") || indicador.equals("igd") || indicador.equals("tcheb") || indicador.equals("pnf") || indicador.equals("all") || indicador.equals("ld") || indicador.equals("con") ){
			
			if(problema == null)
				pftrue= loadParetoFrontFile(true_pareto, m);
			else{
				pftrue= carregarFronteiraPareto(System.getProperty("user.dir"), prob, m);
				j =  problema.getJoelho(n, pftrue);
				l = problema.getLambda(n, pftrue);
			}
		}
		
		String indicators[] = {"gd", "igd", "spread","ld","con", "np"};
		if(!indicador.equals("all")){
			indicators = new String[1];
			indicators[0] = indicador;
		}
			
		
		String caminhoDir = dirExec;
		
		
		if(caminhoDir.lastIndexOf("\\" ) != caminhoDir.length()-1 ){
			if(caminhoDir.lastIndexOf("/" ) != caminhoDir.length()-1)
				caminhoDir+="/";
		}
		
		

		String idExec = front;

		for (int i = 0; i < indicators.length; i++) {
			String indicador = indicators[i];

			Indicador ind = null;
			if(indicador.equals("gd")){
				ind = new GD(m, caminhoDir, idExec, pftrue);
			}
			if(indicador.equals("igd")){
				ind = new IGD(m, caminhoDir, idExec, pftrue);
			}
			if(indicador.equals("ld")){
				ind = new LargestDistance(m, caminhoDir, idExec, pftrue);
			}
			
			if(indicador.equals("con")){
				ind = new Convergence(m, caminhoDir, idExec, pftrue);
			}

			if(indicador.equals("tcheb"))
				ind = new Tchebycheff(m, caminhoDir, idExec, j , l);


			if(indicador.equals("spread"))
				ind = new Spacing(m, caminhoDir, idExec);


			if(indicador.equals("pnf"))
				ind = new PontosNaFronteira(m, caminhoDir, idExec, pftrue);

			if(indicador.equals("np"))
				ind = new NumeroPontos(m, caminhoDir, idExec);

			if(ind!=null){
				ind.preencherObjetivosMaxMin(maxmimObjetivos);
				String frontFile = caminhoDir + "/" + front + "_fronteira.txt";
				//String frontFile = caminhoDir + "/" + front;
				System.out.println("Indicador: " + ind.indicador);
				ind.calcularIndicadorArquivo(frontFile);
			}
		}
	}



	public static ArrayList<PontoFronteira> carregarFronteiraPareto(String dir, String problema, int objetivo){
		ArrayList<PontoFronteira> pftrue = new ArrayList<PontoFronteira>();
		try{
			String arquivo = dir + "/pareto/" + problema.toUpperCase() + "_" + objetivo + "_pareto.txt";
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
	
	public static ArrayList<PontoFronteira> loadParetoFrontFile(String file, int objetivo){
		ArrayList<PontoFronteira> pftrue = new ArrayList<PontoFronteira>();
		try{
			BufferedReader buff = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/pareto/" +file));
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
			problema = new DTLZ1(m,k);
		if(prob.equals("DTLZ2"))
			problema = new DTLZ2(m,k);
		if(prob.equals("DTLZ3"))
			problema = new DTLZ3(m,k);
		if(prob.equals("DTLZ4"))
			problema = new DTLZ4(m,k);
		if(prob.equals("DTLZ5"))
			problema = new DTLZ5(m,k);
		if(prob.equals("DTLZ6"))
			problema = new DTLZ6(m,k);
		if(prob.equals("DTLZ7"))
			problema = new DTLZ7(m,k);
		if(prob.equals("ZTD1")){
			problema = new ZDT1();
			n = 30;
			m = 2;
		}
		if(prob.equals("ZTD2")){
			problema = new ZDT2();
			n = 30;
			m = 2;
		}
		if(prob.equals("ZTD3")){
			problema = new ZDT3();
			n = 30;
			m = 2;
		}
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
					System.err.println("Erro no arquivo de configuracao. Linha: " + linhaString);
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
				
				if(tag.equals("front"))
					front = valor;
				if(tag.equals("true_pareto"))
					true_pareto = valor;

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
				
				if(tag.equals("box_range_beg"))
					box_range_beg = new Double(valor).doubleValue();
				
				if(tag.equals("box_range_end"))
					box_range_end = new Double(valor).doubleValue();
				
				if(tag.equals("swarms"))
					swarms = new Integer(valor).intValue();
				
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
				
				if(tag.equals("pop_swarm"))
					pop_swarm = new Integer(valor).intValue();

				if(tag.equals("rep_swarm"))
					rep_swarm = new Integer(valor).intValue();
				
				if(tag.equals("split_iterations"))
					split_iterations = new Integer(valor).intValue();

				if(tag.equals("problema")){
					prob = valor;
				}

				if(tag.equals("dominance")){
					if(valor.equals("true"))
						dominance = true;
					else
						dominance = false;
				}
				
				if(tag.equals("eval_analysis")){
					if(valor.equals("true"))
						eval_analysis = true;
					else
						eval_analysis = false;
				}
				
				if(tag.equals("reset")){
					if(valor.equals("true"))
						reset = true;
					else
						reset = false;
				}
				
				if(tag.equals("initialize")){
					initialize = valor;
				}
				
				if(tag.equals("shared")){
					if(valor.equals("true"))
						shared = true;
					else
						shared = false;
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
					S = valor;
				if(tag.equals("s_max"))
					S_MAX = new Double(valor).doubleValue();
				if(tag.equals("partesgrid")){
					partesgrid = new Integer(valor).intValue();
				}
				
				if(tag.equals("update")){
					update = new Integer(valor).intValue();
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
				
				if(tag.equals("archiver")){
					tipoArquivo = valor;
					String tipos = "ag ar br crowd ideal pr_id ex_id eucli sigma tcheb rand ub dom eaps eapp mga mga2 spea2 hyper hyp_r";
					if(!tipos.contains(tipoArquivo) && !valor.contains(";")){
						System.err.println("Tipo de arquivamento especificado nao existe");
						System.err.println("Tipos: ag ar br crowd ideal pr_id ex_id eucli sigma tcheb rand ub dom eaps eapp mga mga2 spea2");
						System.exit(0);
					}

				}
			
				
				if(tag.equals("lider")){					
					escolhaLider = valor;
				}
			}
		}
		
		if(n == -1)
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
		buff.append("poda: " + tipoArquivo + "\n");
		buff.append("lider: " + escolhaLider + "\n");
		return buff.toString();
	}

}
