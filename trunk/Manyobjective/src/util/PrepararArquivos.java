package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class PrepararArquivos {
	
	public static int numObjHiper = 10;
	
	
	public void juntarFronteira(String dir, String problema, String objetivo, String[] algoritmos , int exec, String metodo) throws IOException{
		
		boolean entrou = false;
		for (int j = 0; j < algoritmos.length; j++) {
			String arqfronteira = dir + "resultados/" + metodo + "/" + problema + "/" + objetivo + "/" + 
			algoritmos[j] + "/" + metodo + "" + problema + "_" + objetivo + algoritmos[j] + "_fronteira.txt";
			
			PrintStream psFronteira = new PrintStream(arqfronteira);
			
			for (int i = 0; i < exec; i++) {
				String arqfronteiraTemp = dir + "resultados/" + metodo + "/" + problema + "/" + objetivo + "/" + 
				algoritmos[j] + "/" + i + "/" + metodo + "" + problema + "_" + objetivo + "_fronteira.txt";
				//System.out.println(arqfronteiraTemp);
				BufferedReader buff = new BufferedReader(new FileReader(arqfronteiraTemp));
				while(buff.ready()){
					String linha = buff.readLine().replace("\t", " ");
					if(!linha.isEmpty()){
						psFronteira.println(linha);
						entrou = true;
					}
				}
				
				psFronteira.println();
				if(entrou){
					System.out.println(i);
				}
				
				entrou = false;
			}
		}
	}
	
	/**
	 * M�todo que prepara um arquivo contendo um fronteira de pareto para ser executado no PISA
	 * @param dir
	 * @param problema
	 * @param objetivo
	 * @param algoritmos
	 * @param exec
	 * @param metodo
	 * @throws IOException
	 */
	public void inverterMaxMim(String dir, String problema, String objetivo, String[] algoritmos , int exec, String metodo) throws IOException{
		for (int j = 0; j < algoritmos.length; j++) {
			
			
			String arqfronteira = dir + "resultados/" + metodo + "/" + problema + "/" + objetivo + "/" + 
			algoritmos[j] + "/" + metodo + "" + problema + "_" + objetivo + algoritmos[j] + "_fronteira.txt";
			
			System.out.println(arqfronteira);
			
			/*BufferedReader buff = new BufferedReader(new FileReader(arqfronteira));
			double maior = 0;
			while(buff.ready()){
				String linha = buff.readLine();
				if(!linha.isEmpty()){
					String[] valores = linha.split(" ");
					for (int i = 0; i < valores.length; i++) {
						maior = Math.max(maior, new Double(valores[i].replace(',', '.')).doubleValue());
					}
				}
			}*/
			
			BufferedReader buff = new BufferedReader(new FileReader(arqfronteira));
			
			PrintStream ps = new PrintStream("teste_" + algoritmos[j] + ".txt");
			while(buff.ready()){
				String linha = buff.readLine();
				if(linha.isEmpty()){
					ps.println();
				} else {
					String[] valores = linha.split(" ");
					for (int i = 0; i < valores.length; i++) {
						//double novo_valor = Math.max(0.0001, maior - new Double(valores[i].replace(',', '.')).doubleValue());
						Double novo_valor = new Double(valores[i].replace(',', '.')).doubleValue();
						if(novo_valor>4)
							System.out.println(novo_valor);
						if((novo_valor < 0.001) && novo_valor!=0){
							DecimalFormat formatador = new DecimalFormat("0.0000000000000000");  
							
							ps.print(formatador.format(novo_valor) + " ");
						} else
							ps.print(novo_valor + " ");
					}
					ps.println();
				}
			}
		}
	}
	
	public void gerarComando(String dirEntrada, String dirSaida, String problema, int objetivo, String[] algoritmos , int exec, String metodo,String ind ) throws IOException{
		
		Double[][] valores = new Double[algoritmos.length][exec];
		

		BufferedReader buff;


		for (int j = 0; j < algoritmos.length; j++) {

			String arq = dirEntrada + "resultados/" + metodo + "/" + problema + "/" + objetivo + "/" +			
			algoritmos[j] + "/" + metodo + "_" + problema + "_" + objetivo + "_" + algoritmos[j] + "_" + ind + ".txt";

		

			System.out.println(arq);


			buff = new BufferedReader(new FileReader(arq));
			int tam = 0;
			while(buff.ready() && tam<exec){
				String linha = buff.readLine();
				if(!linha.isEmpty()){
					Double val = new Double(linha);
					//try{
					valores[j][tam++] = val;
					//} catch(ArrayIndexOutOfBoundsException x){x.printStackTrace();}
				}
			}
		}  
		
		
		
		for (int i = 0; i < algoritmos.length; i++) {
			StringBuffer comando = new StringBuffer();
			String id = metodo + "_" + problema + "_" + objetivo + "_" + algoritmos[i] + "_" + ind;
			String arqfronteira = dirSaida + "resultados/" + metodo + "/" + problema + "/" + objetivo + "/" + 
			algoritmos[i] + "/" +  id + "_comando.txt";
			comando.append(id + "<-c(");
			for (int j = 0; j < exec; j++) {
				comando.append(valores[i][j] + ",");
			}
			
			comando.deleteCharAt(comando.length()-1);
			comando.append(")\n");
			
			PrintStream ps = new PrintStream(arqfronteira);
			
			ps.println(comando);
		}
			
	}
	
		
	
	public void preparArquivosEval(String dirEntrada, String dirSaida, String problema, String objetivo, String[] algoritmos , int exec, String metodo[], PrintStream psSaida) throws IOException{
		Double[][] valores = new Double[algoritmos.length][1];
		
		String ind = "eval";

		BufferedReader buff;


		for (int j = 0; j < algoritmos.length; j++) {

			String arq = dirEntrada + "resultados/" + metodo[j] + "/" + problema + "/" + objetivo + "/" +			
			algoritmos[j] + "/" + metodo[j] + "_" + problema + "_" + objetivo + "_" + algoritmos[j] + "_" + ind + ".txt";

			System.out.println(arq);


			buff = new BufferedReader(new FileReader(arq));
			int tam = 0;
			while(buff.ready() && tam<exec){
				String linha = buff.readLine();
				if(!linha.isEmpty()){
					if(linha.contains(","))
						linha = linha.replace(',', '.');
					Double val = new Double(linha);
					valores[j][0] = val;
					break;
				}
			}
		}  
		
		
		
		
		String caminhoDirExec = dirSaida + "medidas/";
		
		

		if(psSaida == null)
			psSaida = new PrintStream(caminhoDirExec + "medidas/" + metodo[0] + problema + "_"+ ind + "_" + objetivo + "_indicadores.txt");
		

		for (int i = 0; i < algoritmos.length; i++) {
			try{
				psSaida.print(valores[i][0] + "\t");		
			}catch(NullPointerException ex){ex.printStackTrace();}
		}


		psSaida.println();
	}


	
	public void preparArquivosIndicadores(String dirEntrada, String dirSaida, String problema, String objetivo, String[] algoritmos, int exec, String metodo[], String ind, PrintStream psSaida) throws IOException{
		Double[][] valores = new Double[algoritmos.length][exec];
		

		BufferedReader buff;


		for (int j = 0; j < algoritmos.length; j++) {

			String arq = dirEntrada + "resultados/" + metodo[j] + "/" + problema + "/" + objetivo + "/" +			
			algoritmos[j] + "/" + metodo[j] + "_" + problema + "_" + objetivo + "_" + algoritmos[j] + "_" + ind + ".txt";
			
			//trucamento dos valores para 8 casas decimais
			/*String arqComando2 = dir + "resultados/" + metodo + "/" + problema + "/" + objetivo + "/" +			
			algoritmos[j] + "/" + metodo + "_" + problema + "_" + objetivo + "_" + algoritmos[j] + "_" + ind + "_comando.txt";
			
			PrintStream psComando2 = new PrintStream(arqComando2);
			
			StringBuffer comando2 = new StringBuffer();
			
			comando2.append(metodo + "_" + problema + "_" + objetivo + "_" + algoritmos[j] + "_" + ind  +"<- c(");*/

		

			System.out.println(arq);


			buff = new BufferedReader(new FileReader(arq));
			int tam = 0;
			while(buff.ready() && tam<exec){
				String linha = buff.readLine();
				if(!linha.isEmpty()){
					if(linha.contains(","))
						linha = linha.replace(',', '.');
					Double val = new Double(linha);
					//try{
					valores[j][tam++] = val;
					
					/*BigDecimal b = new BigDecimal(val);		 
					val = (b.setScale(8, BigDecimal.ROUND_UP)).doubleValue();
					comando2.append(val + ",");*/
					//} catch(ArrayIndexOutOfBoundsException x){x.printStackTrace();}
				}
			}
			
			/*comando2.deleteCharAt(comando2.length()-1);
			comando2.append(")");
			psComando2.println(comando2);*/
		}  
		
		
		
		
		String caminhoDirExec = dirSaida + "medidas/";
		
		

		if(psSaida == null)
			psSaida = new PrintStream(caminhoDirExec + "medidas/" + metodo[0] + problema + "_"+ ind + "_" + objetivo + "_indicadores.txt");
		




		for(int j = 0; j<exec; j++){
			for (int i = 0; i < algoritmos.length; i++) {
				try{
					psSaida.print(valores[i][j] + "\t");		
				}catch(NullPointerException ex){ex.printStackTrace();}
			}


			psSaida.println();
		}


	}
	
	public void preparArquivoTempo(String dirEntrada, String dirSaida, String problema, String objetivo, String[] algoritmos , int exec, String metodo[], PrintStream psSaida) throws IOException{
		Double[][] valores = new Double[algoritmos.length][exec];
		

		BufferedReader buff;


		for (int j = 0; j < algoritmos.length; j++) {

			String arq = dirEntrada + "resultados/" + metodo[j] + "/" + problema + "/" + objetivo + "/" +			
			algoritmos[j] + "/" + metodo[j] + "_" + problema + "_" + objetivo + "_"+ algoritmos[j] + "_texec.txt";

		

			System.out.println(arq);


			buff = new BufferedReader(new FileReader(arq));
			int tam = 0;
			while(buff.ready() && tam<exec){
				String linha = buff.readLine();
				if(!linha.isEmpty()){
					String[] tempos = linha.split("\t");
					Double tInicio = new Double(tempos[1])/1000; 
					Double tFim = new Double(tempos[2])/1000;
					Double val = new Double(tFim - tInicio);
					//try{
					valores[j][tam++] = val;
					//} catch(ArrayIndexOutOfBoundsException x){x.printStackTrace();}
				}
			}
		}  
		
		


		if(psSaida == null)
			psSaida = new PrintStream(dirSaida + "medidas/" + metodo[0] + problema + "_tempo_" + objetivo + "_indicadores.txt");
		



		for(int j = 0; j<exec; j++){
			for (int i = 0; i < algoritmos.length; i++) {
				try{
					psSaida.print(valores[i][j].toString().replace(".", ",") + "\t");		
				}catch(NullPointerException ex){ex.printStackTrace();}
			}


			psSaida.println();
		}


	}
	

	public void preparArquivosIndicadoresTodos(String dirEntrada, String dirSaida, String problema, String[] algoritmos , int exec, String metodo[], String ind, int[] objs, String[] algs, String idMetodo) throws IOException{
		
		String caminhoDirExec = dirSaida + "medidas/";
		
		File diretorio = new File(caminhoDirExec);
		diretorio.mkdirs();

		
		PrintStream psSaida = new PrintStream(caminhoDirExec + metodo[0] + problema + "_"+ ind + "_" + idMetodo + "_indicadores_all.txt");
		
		for (int i = 0; i < objs.length; i++) {
			System.out.println(objs[i]);
			
			
			String objetivo = objs[i] + "";
			
			if(ind.equals("tcheb"))
				preparArquivosTcheb(dirEntrada, dirSaida,  problema, objetivo, algs, exec, metodo);
			else{
				if(ind.equals("tempo")){
					preparArquivoTempo(dirEntrada, dirSaida, problema, objetivo, algoritmos, exec, metodo, psSaida);
					psSaida.println("\n\n\n\n\n\n");
				} else{
					if(ind.equals("eval"))
						preparArquivosEval(dirEntrada, dirSaida, problema, objetivo, algoritmos, exec, metodo, psSaida);
					else{
						preparArquivosIndicadores(dirEntrada, dirSaida, problema, objetivo, algoritmos, exec, metodo, ind, psSaida);
						psSaida.println("\n\n\n\n\n\n");
					}

				//preparArquivosComandosFriedman(dir, dir2,  problema, objetivo, algs, exec, metodo, ind);
				}
			}
			
		}
		
	}

	public void preparArquivosTcheb(String dirEntrada, String dirSaida, String problema, String objetivo, String[] algoritmos , int exec, String metodo[]) throws IOException{

		BufferedReader buff;
		
		PrintStream psSaida = new PrintStream(dirSaida + "medidas/" + metodo[0] + "_" + problema + "_tchebycheff_" + objetivo + "_indicadores.txt");
		
		ArrayList<ArrayList<String>> todosArquivos = new ArrayList<ArrayList<String>>();
		
		double maiorTamanho = Double.NEGATIVE_INFINITY;
		
		double soma[] = new double[algoritmos.length];
		
		for (int i = 0; i < soma.length; i++) {
			soma[i] = 0;
		}

		for (int j = 0; j < algoritmos.length; j++) {

			String arq = dirEntrada + "resultados/" + metodo[j] + "/" + problema + "/" + objetivo + "/" +			
			algoritmos[j] + "/" + metodo[j] + "_" + problema + "_" + objetivo + "_" + algoritmos[j] + "_tchebycheff.txt";

		

			System.out.println(arq);
			
			ArrayList<String> arquivo = new ArrayList<String>();
			
			arquivo.add("S = " + algoritmos[j] + "\t");
			
			buff = new BufferedReader(new FileReader(arq));
			
			
			
			int tam = 1;
			while(buff.ready()  && tam<exec){
				String linha = buff.readLine();
				
				linha = linha.substring(0, linha.length());
				if(!linha.isEmpty()){
					arquivo.add(linha);
					tam++;
					String[] valores = linha.split("\t");
					try{
						soma[j] += new Double(valores[1]).doubleValue();
					}
					catch(ArrayIndexOutOfBoundsException x){x.printStackTrace();}
					
				}
			}
			
			todosArquivos.add(arquivo);
			maiorTamanho = Math.max(maiorTamanho, tam);
			
			
		}  
		
		//ArrayList<ArrayList<String>> todosArquivosNorm = normalizarTcheb(todosArquivos, soma);
		
		todosArquivos = normalizarTcheb(todosArquivos, soma);

		for(int l =0; l<maiorTamanho; l++){
			for (Iterator<ArrayList<String>> iterator = todosArquivos.iterator(); iterator.hasNext();) {
				ArrayList<String> arrayList = (ArrayList<String>) iterator.next();
				if(l<arrayList.size()){
					psSaida.print( arrayList.get(l) + "\t");
				} else{
					psSaida.print("\t\t");
				}
				psSaida.print("\t");
			}
			
			psSaida.println();
		}
		
		



		

	}

	private ArrayList<ArrayList<String>> normalizarTcheb(ArrayList<ArrayList<String>> todosArquivos,
			double[] soma) {
		
		
		
		ArrayList<ArrayList<String>> saida = new ArrayList<ArrayList<String>>();
		int k = 0;
		for (Iterator<ArrayList<String>> iterator = todosArquivos.iterator(); iterator.hasNext();) {
			ArrayList<String> arrayList = (ArrayList<String>) iterator.next();
			
			ArrayList<String> novoAlg = new ArrayList<String>();
			
			for (Iterator<String> iterator2 = arrayList.iterator(); iterator2.hasNext();) {
				String linha = (String) iterator2.next();
				String valores[] = linha.split("\t");
				if(valores.length !=1){		
					double valor = new Double(valores[1]).doubleValue();
					double novoValor = valor/soma[k];
					String novaLinha = valores[0] + "\t" + new Double( novoValor).toString().replace('.', ',');
					novoAlg.add(novaLinha);
				} else
					novoAlg.add(linha);
				
			}
			
			saida.add(novoAlg);
			
			k++;
		}
		
		return saida;
	}
	
	public void preparArquivosComandosWilcox(String dir, String problema, String objetivo, String[] algoritmos , int exec, String metodo) throws IOException{
		

		BufferedReader h, s, g;
		
		PrintStream hypSaida = new PrintStream(dir + "resultados/hyper_" + objetivo + "_comando.txt");
		PrintStream spreSaida = new PrintStream(dir + "resultados/spread_" + objetivo + "_comando.txt");
		PrintStream igdSaida = new PrintStream(dir + "resultados/igd_" + objetivo + "_comando.txt");
		
		StringBuffer comandosHyp = new StringBuffer();
		StringBuffer comandosSpread = new StringBuffer();
		StringBuffer comandosigd = new StringBuffer();
		
		String algCompHyp = metodo + problema + "_" + objetivo + "0.5_hipervolume";
		String algCompSpread = metodo + problema + "_" + objetivo + "0.5_spread";
		String algCompigd = metodo + problema + "_" + objetivo + "0.5_igd";
		
		int obj = Integer.parseInt(objetivo);

		for (int j = 0; j < algoritmos.length; j++) {
			String arqHyp = "";
			if(obj<=numObjHiper)
				arqHyp = dir + "resultados/" + metodo + problema + "/" + problema + "/" + objetivo + "/" + 
			algoritmos[j] + "/" + metodo + "" + problema + "_" + objetivo + algoritmos[j] + "_hipervolume_comando.txt";
			String arqSpread = dir + "resultados/" + metodo + problema + "/" + problema + "/" + objetivo + "/" + 
			algoritmos[j] + "/" + metodo + "" + problema + "_" + objetivo + algoritmos[j] + "_spread_comando.txt";
			String arqigd = dir + "resultados/" + metodo + problema + "/" + problema + "/" + objetivo + "/" + 
			algoritmos[j] + "/" + metodo + "" + problema + "_" + objetivo + algoritmos[j] + "_igd_comando.txt";
			
			if(!algoritmos[j].equals("0.5")){
				String algHyp = metodo + problema + "_" + objetivo + algoritmos[j] + "_hipervolume";
				String algSpread = metodo + problema + "_" + objetivo + algoritmos[j] + "_spread";
				String algigd = metodo + problema + "_" + objetivo + algoritmos[j] + "_igd";
				if(obj<=numObjHiper)
					comandosHyp.append("wilcox.test(" + algHyp + "," + algCompHyp + ")\n");
				comandosSpread.append("wilcox.test(" + algSpread + "," + algCompSpread + ")\n");
				comandosigd.append("wilcox.test(" + algigd + "," + algCompigd + ")\n");
			}
			
			
			if(obj<=numObjHiper)
				h = new BufferedReader(new FileReader(arqHyp));
			else
				h = new BufferedReader(new FileReader(arqSpread));
			s = new BufferedReader(new FileReader(arqSpread));
			g = new BufferedReader(new FileReader(arqigd));
			while(s.ready()){
				if(obj<=numObjHiper)
					hypSaida.println(h.readLine());
				spreSaida.println(s.readLine());
				igdSaida.println(g.readLine());
			}
			
		}  
		
		if(obj<=numObjHiper)
			hypSaida.println();
		spreSaida.println();
		igdSaida.println();
		
		if(obj<=numObjHiper)
			hypSaida.println(comandosHyp);
		spreSaida.println(comandosSpread);
		igdSaida.println(comandosigd);

			
	}
	
public void preparArquivosComandosFriedman(String dir, String dir2, String problema, String objetivo, String[] algoritmos , int exec, String metodo[], String ind) throws IOException{
		

		BufferedReader buff;
		

		PrintStream psSaida = new PrintStream(dir2 + "medidas/" + metodo[0] + problema + "_" + ind +"_" + objetivo + "_comando_friedman.txt");

		System.out.println(dir2 + "medidas/" + metodo[0] + problema + "_" + ind +"_" + objetivo + "_comando_friedman.txt");
		
		StringBuffer comandos = new StringBuffer();
		
		
		StringBuffer comandosBox = new StringBuffer();
		
		
		
		comandos.append("require(pgirmess)\n AR1 <-cbind(");
		
		
		comandosBox.append("boxplot(");
		

		for (int j = 0; j < algoritmos.length; j++) {
			String arq =  dir + "resultados/" + metodo[j]  + "/" + problema + "/" + objetivo + "/" + 
			algoritmos[j] + "/" + metodo[j] + "_" + problema + "_" + objetivo + "_" + algoritmos[j] + "_" + ind +"_comando.txt";
			
			
			
			String alg = metodo[j] + "_" + problema + "_" + objetivo + "_" + algoritmos[j] + "_" + ind;
			
			
			comandos.append(alg + ",");
			
			
			
			comandosBox.append(alg + ",");
			
			
			
			buff = new BufferedReader(new FileReader(arq));
			
			
			
			while(buff.ready()){
			
				psSaida.println(buff.readLine());
				
			}
			
		}  
		
		
		psSaida.println();
		
		
		comandosBox.deleteCharAt(comandosBox.length()-1);
		comandosBox.append(")\n");
		
		
		
		comandos.deleteCharAt(comandos.length()-1);
		comandos.append(")\n");
		
		
		
		comandos.append(	"result<-friedman.test(AR1)\n\n" +
							 "m<-data.frame(result$statistic,result$p.value)\n" +
							 "write.csv2(m,file=\"" +  dir2 + "medidas/result_"+ metodo[0] + problema + objetivo+ "_" + ind+ ".csv\")\n\n" +
							 "pos_teste<-friedmanmc(AR1)\n" +
							 "write.csv2(pos_teste,file=\"" + dir2 + "medidas/friedman_"+ metodo[0] + problema+ objetivo+ "_" + ind +".csv\")");
		
		
		
		
		psSaida.println(comandos);
		psSaida.println(comandosBox);
		
	
		
	}


	
	public static void main(String[] args) {
		PrepararArquivos pre = new PrepararArquivos();

		//String dir = "/home/andre/gemini/doutorado/experimentos/poda/";		
		String dirEntrada = "/home/andrebia/gemini/doutorado/experimentos/multi/";
		String dirSaida = "/media/Dados/Andre/multi/";
		//String dir2 = "/home/andre/gemini/doutorado/experimentos/poda/";
		//int objetivo = 2;
		String problema  = "DTLZ2";
		String lider = "tb";
		
		//String[] algs = {"tb_mga_3_ctd","tb_mga_5_ctd","tb_mga_10_ctd","tb_mga_20_ctd", "tb_mga_30_ctd", "tb_mga_3_ctd_r","tb_mga_5_ctd_r","tb_mga_10_ctd_r","tb_mga_20_ctd_r", "tb_mga_30_ctd_r"};
		//String[] algs = {"tb_mga_3_ext","tb_mga_5_ext","tb_mga_10_ext", "tb_mga_30_ext"};
		//String[] algs = {"0.5_tb_ideal","0.5_tb_mga"};
		String[] algs = {"tb_mga_30_ext","tb_mga_30_ctd","tb_mga_30_rnd","0.5_tb_ideal","0.5_tb_mga"};
		//String metodo[] = {"imulti","imulti","imulti","imulti"};
		String metodo[] = {"imulti","imulti","imulti","smopso","smopso"};
		//String metodo = "smopso";
		
		
		//int objs[] = {3,5,10,15,20,30};
		int objs[] = {3,5,10,20,30};
		int exec = 30;

		try{

			
			
			//pre.preparArquivosIndicadoresTodos(dirEntrada, dirSaida, problema, algs, exec, metodo, "gd", objs, algs,lider);
			//pre.preparArquivosIndicadoresTodos(dirEntrada, dirSaida, problema, algs, exec, metodo, "igd", objs, algs,lider);
			//pre.preparArquivosIndicadoresTodos(dirEntrada, dirSaida, problema, algs, exec, metodo, "spread", objs, algs,lider);
			//pre.preparArquivosIndicadoresTodos(dirEntrada, dirSaida, problema, algs, exec, metodo, "ld", objs, algs,lider);
			pre.preparArquivosIndicadoresTodos(dirEntrada, dirSaida, problema, algs, exec, metodo, "con", objs, algs,lider);
			//pre.preparArquivosIndicadoresTodos(dirEntrada, dirSaida, problema, algs, exec, metodo, "eval", objs, algs,lider);
			//pre.preparArquivosIndicadoresTodos(dirEntrada, dirSaida, problema, algs, exec, metodo, "tcheb", objs, algs,lider);
			
			for (int i = 0; i < objs.length; i++) {
				System.out.println(objs[i]);
				
				//pre.preparArquivosComandosFriedman(dirEntrada, dirSaida,  problema, ""+objs[i], algs, exec, metodo, "gd");
				//pre.preparArquivosComandosFriedman(dirEntrada, dirSaida,  problema, ""+objs[i], algs, exec, metodo, "igd");
				//pre.preparArquivosComandosFriedman(dirEntrada, dirSaida,  problema, ""+objs[i], algs, exec, metodo, "spread");
				//pre.preparArquivosComandosFriedman(dirEntrada, dirSaida,  problema, ""+objs[i], algs, exec, metodo, "ld");
				pre.preparArquivosComandosFriedman(dirEntrada, dirSaida,  problema, ""+objs[i], algs, exec, metodo, "con");
				//pre.gerarComando(dirEntrada, dirSaida, problema, objs[i], algs, exec, metodo, "hypervolume");
				//pre.preparArquivosComandosFriedman(dirEntrada, dirSaida,  problema, ""+objs[i], algs, exec, metodo, "hypervolume");
			}
			
			

			//pre.juntarFronteira(dir, problema, objetivo, algs, exec, metodo);
			//pre.inverterMaxMim(dir, problema, objetivo, algs, exec, metodo);
			//pre.preparArquivosIndicadores(dir, problema, ""+objetivo, algs, exec, metodo, "gd", null);
			//pre.preparArquivosIndicadores(dir, problema, ""+objetivo, algs, exec, metodo, "igd", null);
			//pre.preparArquivosIndicadores(dir, problema, ""+objetivo, algs, exec, metodo, "spread", null);
			//pre.preparArquivosIndicadores(dir, problema, ""+objetivo, algs, exec, metodo, "pnf", null);
			//pre.preparArquivosIndicadores(dir, problema, ""+objetivo, algs, exec, metodo, "np", null);
			//pre.preparArquivoTempo(dir, problema, ""+objetivo, algs, exec, metodo,  null);

			//pre.preparArquivosComandosFriedman(dir, dir2,   problema, ""+objetivo, algs, exec, metodo, "gd");
			//pre.preparArquivosComandosFriedman(dir, dir2,  problema, ""+objetivo, algs, exec, metodo, "igd");
			//pre.preparArquivosComandosFriedman(dir, dir2,  problema, ""+objetivo, algs, exec, metodo, "spread");
			//pre.preparArquivosComandosFriedman(dir, dir2,  problema, ""+objetivo, algs, exec, metodo, "pnf");
			//pre.preparArquivosComandosFriedman(dir, dir2,  problema, ""+objetivo, algs, exec, metodo, "np");

			
			//pre.preparArquivosComandosFriedman(dir, dir2,  problema, ""+objetivo, algs, exec, metodo, "pnf");
			//pre.preparArquivosComandosFriedman(dir, dir2,  problema, ""+objetivo, algs, exec, metodo, "np");
			
			

		} catch (IOException ex){ex.printStackTrace();}
		
		
	}

}
