package classificadores;

import java.io.FileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import regra.Regra;

import kernel.DadosExperimentos;
import kernel.DadosExecucao;
import kernel.InstanceVotacao;
import kernel.MatrizConfusao;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.NNge;
import weka.classifiers.trees.J48;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

public class Classificadores {
	
	
	public void treinarRede(Instances dados, Classifier classif) throws Exception{
		classif.buildClassifier(dados);
	}
	
	public double[] avaliarModelo(Instances dadosTeste, Classifier classif, int classIndex) throws Exception{
		
		Evaluation eval = new Evaluation(dadosTeste);
		eval.evaluateModel(classif,dadosTeste);
		
		//ThresholdCurve tc = new ThresholdCurve();
		// Instances result = tc.getCurve(eval.predictions(), classIndex);
		
		double auc = eval.areaUnderROC(classIndex);
		/*double fmeasure = eval.fMeasure(classIndex);
		double precision = eval.precision(classIndex);
		double recall = eval.recall(classIndex);
		*/
		
		
		//double accuracy = (TP+TN)/(TP+TN+FP+FN);
		
		double[] retorno = new double[5];
		retorno[0] = auc;
		retorno[1] = eval.numTruePositives(classIndex);
		retorno[2] = eval.numFalsePositives(classIndex);
		retorno[3] = eval.numTrueNegatives(classIndex);
		retorno[4] = eval.numFalseNegatives(classIndex);
		return retorno;
		

		
		
	}
	


	
/*
	private void gerarComando(String nomeBase, String metodo, ArrayList<Double> aucs) throws FileNotFoundException {
		PrintStream ps = new PrintStream(metodo+"_"+nomeBase+".txt");
		
		StringBuffer comando = new StringBuffer();
		comando.append(metodo);
		comando.append(nomeBase.substring(0,3) + "<-c(");
		for (Iterator iter = aucs.iterator(); iter.hasNext();) {
			Double auc = (Double) iter.next(); 
			ps.println(auc.toString().replace(".",","));
			comando.append(auc + ",");
		}
		
		comando.deleteCharAt(comando.length()-1);
		
		comando.append(")");
		
		ps.println();
		ps.println(comando);
	}
*/
	/**
	 * M�todo que executar um classificador passado como par�metro e retorna a o valor da AUC de cada execucao para cada fold
	 * @param nomeBase Nome da base de dados
	 * @param caminhoBase Caminho da base de dados
	 * @param numFolds N�mero de folds da base de dados
	 * @param numExec N�mero de execu��o do algoritmo
	 * @param metodo M�todo que sera utilizado (rn, bayes, naive ou smo)
	 * @return Cole��o com os valores de AUC das execu��es
	 * @throws Exception
	 */
	private  void executarClassificacao(String nomeBase, String caminhoBase, int numFolds, int numExec, String metodo) throws Exception  {
		
		RBFKernel rbf = new RBFKernel();
		MultilayerPerceptron mlp = null;

		SMO smo = null;
		J48 j48 = null;
	
		
		
		
		Classifier classif = null;
		
		DadosExperimentos dadosBase = new DadosExperimentos();
		dadosBase.nomeBase = nomeBase;
		dadosBase.metodo = metodo;
		for(int j = 0; j<numExec;j++){
			
			if(metodo.equals("rn")){
				mlp = new MultilayerPerceptron();
				mlp.setRandomSeed(System.currentTimeMillis());
				classif = mlp;
			}
			if(metodo.equals("bayes")){
				classif = new BayesNet();
			}
			if(metodo.equals("naive")){
				classif =  new NaiveBayes();
			}
			if(metodo.equals("c45")){
				classif = new J48();
			}
			if(metodo.equals("c45np")){
				j48 = new J48();
				j48.setUnpruned(true);
				classif = j48;
			}
			if(metodo.equals("ripper")){
				classif = new JRip();
			}
			if(metodo.equals("nnge")){ 
				classif = new NNge();;
			}
			if(metodo.equals("smo")){
				smo = new SMO();
				smo.setKernel(rbf);
				SelectedTag tag = new SelectedTag(smo.FILTER_NONE, smo.TAGS_FILTER);
				smo.setFilterType(tag);
				smo.setRandomSeed((int)System.currentTimeMillis());
				classif = smo;
			}
			
			System.out.println("Execucao: " + j);
			for(int i = 0; i<numFolds; i++){
				System.out.println("Fold: " + i);
				String arquivoTreinamento = caminhoBase + nomeBase + "/it"+i+"/" + nomeBase + "_data.arff";
				System.out.println("Base de treinamento: " + arquivoTreinamento);
				
				Reader reader = new FileReader(arquivoTreinamento);
				Instances dados = new Instances(reader);
				dados.setClassIndex(dados.numAttributes()-1);
				System.out.println("Instancias de treinamento: " + dados.numInstances());
				
				treinarRede(dados, classif);
				
				String arquivoTeste = caminhoBase + nomeBase + "/it"+i+"/" + nomeBase + "_test.arff";
				reader = new FileReader(arquivoTeste);
				Instances dadosTeste = new Instances(reader);
				System.out.println("Base de teste: " + arquivoTeste);
				System.out.println("Instancias de teste: " + dadosTeste.numInstances());
				dadosTeste.setClassIndex(dadosTeste.numAttributes()-1);
				
				System.out.println("AUC: " + obterROC(classif, dadosTeste, metodo));
				
				int classIndex = 0;
				double medidas[] = avaliarModelo(dadosTeste, classif,classIndex);
				
				MatrizConfusao confusao = new MatrizConfusao();
				confusao.tp = medidas[1];
				confusao.fp = medidas[2];
				confusao.tn = medidas[3];
				confusao.fn = medidas[4];
				
				DadosExecucao fold = new DadosExecucao(nomeBase, i, j, medidas[0], 0, confusao);
				
				dadosBase.addFold(fold);
				
				
			}
			
			String caminhoDir = System.getProperty("user.dir");
			
			String diretorio = caminhoDir +  "/resultados/" + metodo +"/" + nomeBase +"/";
			File dir = new File(diretorio);
			dir.mkdirs();
			
			 
			dadosBase.gerarArquivosMedidas(diretorio,metodo+"_"+ nomeBase + "_medidas",metodo+"_"+ nomeBase + "_comandos", metodo+"_"+ nomeBase + "_confusao");
		}
		
	}
	
	public double obterROC(Classifier classif, Instances dadosTeste, String metodo) throws Exception{
		
			ArrayList<InstanceVotacao> votos = new ArrayList<InstanceVotacao>();
			TreeSet<Double> limiares = new TreeSet<Double>();
			
			for(int i=0; i<dadosTeste.numInstances(); i++){
				Instance dado = dadosTeste.instance(i);
				double prediction = (classif.distributionForInstance(dado))[0];		
				InstanceVotacao temp= new InstanceVotacao(dado, prediction);
				votos.add(temp);
				limiares.add(new Double(prediction));
				
			}
			
			
			
			double[][] d = construirROC(dadosTeste, votos, limiares);
			
			PrintStream psROC = new PrintStream("roc_" + metodo);
			
			for (int i = 0; i < d[0].length; i++) {
				psROC.println(new Double(d[1][i]).toString().replace('.', ',') + "\t" + new Double(d[0][i]).toString().replace('.', ','));
			}
//			System.out.println("limiares.size = " + limiares.size());
			
			
			/*CurvaROC positive = new CurvaROC(d[0], d[1], "Curva ROC - Positive", limiares);
			positive.setVisible(true);
			positive.pack();*/
			
			//Calcular Area da classe positiva
			double area = calcularArea(d[1], d[0]);
			
		
			return area;

	}

public double calcularArea(double[] vetorX, double[] vetorY){

	double h;
	double area = 0;
	for(int i = 0  ; i<vetorX.length-1; i++){

		h = Math.abs(vetorX[i]-vetorX[i+1]);

		if(h>0){
			double temp = ((vetorY[i+1]+vetorY[i])/2.0) * h;
			area+= temp;
		}

	}

	return area;
}

public double[][] construirROC(Instances dadosTeste, ArrayList<InstanceVotacao> votos, TreeSet<Double> limiares){

	String classePositiva = dadosTeste.classAttribute().value(0);
	String classeNegativa = dadosTeste.classAttribute().value(1);

	int i = 0;
	for (Iterator iter = votos.iterator(); iter.hasNext(); i++) {
		InstanceVotacao element = (InstanceVotacao) iter.next();
		element.codigo = i;

	}
	Collections.sort(votos);
	//	System.out.println("::::: construirROC() - limiares.size() = " + limiares.size());

	double[][] classesSugeridasPositivos = new double[limiares.size()][dadosTeste.numInstances()];

	i = 0;
	for (Iterator iter = limiares.iterator(); iter.hasNext(); i++) {
		Double limiar = (Double) iter.next();
		int j = 0;
		for (Iterator iterator = votos.iterator(); iterator.hasNext(); j++) {
			InstanceVotacao temp = (InstanceVotacao) iterator.next();
			if(temp.votacao<limiar.doubleValue()){
				classesSugeridasPositivos[i][j] = temp.exemplo.classAttribute().indexOfValue(classeNegativa);

			} else{
				classesSugeridasPositivos[i][j] = temp.exemplo.classAttribute().indexOfValue(classePositiva);

			}
		}
	}

	double[] tpr = new double[classesSugeridasPositivos.length];
	double[] fpr = new double[classesSugeridasPositivos.length];


	for(int k = 0; k<classesSugeridasPositivos.length; k++){
		int j = 0;
		int tp = 0;
		int fp = 0;

		int p = 0;
		int n = 0;

		for (Iterator iter = votos.iterator(); iter.hasNext(); j++) {
			InstanceVotacao element = (InstanceVotacao) iter.next();
			double classeReal = element.exemplo.classValue();

			double classeSugeridaPositiva = classesSugeridasPositivos[k][j];
			if(classeSugeridaPositiva == element.exemplo.classAttribute().indexOfValue(classePositiva)){

				if(classeSugeridaPositiva == classeReal){
					tp++;
				} else {
					fp++;
				}
			}


			if(classeReal == element.exemplo.classAttribute().indexOfValue(classePositiva))
				p++;
			else 
				n++;
		}


		if(p!=0){
			tpr[k] = (double)tp/(double)p;

		} else {
			tpr[k] = 0;
		}
		if(n!=0){
			fpr[k] = (double)fp/(double)n;	
		} else{
			fpr[k] = 0;	
		}

	}

	double[][] result = new double[2][tpr.length];
	result[0] = tpr;
	result[1] = fpr;
	return result;

}
	
	public static void main(String[] args) {
		String nomeBase = args[0];
		String caminhoBase = args[1];
		
		int numFolds = new Integer(args[2]);
		int numExec = new Integer(args[3]);
		
		String metodo = args[4];
		
		/*String nomeBase = "kc1_class_defeito_numerico";
		String caminhoBase = "C:/Andre/bases/book/";
		
		int numFolds = 10;
		int numExec = 1;*/
		
		Classificadores c = new Classificadores();
		try{	
			c.executarClassificacao(nomeBase, caminhoBase, numFolds, numExec, metodo);

		} catch (Exception ex){ex.printStackTrace();}
		
		
			
	}
	
}
