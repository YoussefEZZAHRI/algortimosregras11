package indicadores;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;



/**
 * Classe que represta o m�todo General Distance proposto por Veldhuizen
 * @author Andre
 *
 */
public class Tchebycheff extends Indicador {
	
	public double[] joelho, lambda; 
	
	/*
	 * O indicador deve receber como entrada a fronteia de pareto real, PFtrue
	 */
	public Tchebycheff(int m, String caminho, String idExec, double[] j, double[] l){
		super(m, caminho, idExec);
		indicador = "tchebycheff";
		
		joelho = j;
		
		lambda = l;
	}
	
	public double calcular() {
		return 0;
	}
	

	
	public void calcularTchebycheff(ArrayList<ArrayList<PontoFronteira>> fronteiras) throws IOException{
		iniciarArquivosSaida();
		
		HashMap<Double, Integer> histograma = new HashMap<Double, Integer>();
		
		for (Iterator<ArrayList<PontoFronteira>> iterator1 = fronteiras.iterator(); iterator1.hasNext();) {
			fronteira =  iterator1.next();

			double[] distancias = new double[fronteira.size()];

			int i = 0;
			for (Iterator<PontoFronteira> iterator = fronteira.iterator(); iterator.hasNext();) {
				PontoFronteira pf =  iterator.next();
				distancias[i++] = distanciaTchebycheff(pf.objetivos, joelho, lambda); 
			}
			int decimalPlace = 1;

			
			for (int j = 0; j < distancias.length; j++) {				  
				BigDecimal bd = new BigDecimal(distancias[j]);     
				bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
				Double chave = new Double(bd.doubleValue());		     
				if(histograma.containsKey(chave)){
					Integer cont = histograma.get(chave);		
					int novoValor = cont.intValue() + 1;
					histograma.put(chave, novoValor);
				} else
					histograma.put(chave, 1);
			}
		}
		
		Set<Double> keys = histograma.keySet();
		ArrayList<Double> keysOrdenados  = new ArrayList<Double>();
		keysOrdenados.addAll(keys);
		Collections.sort(keysOrdenados);

		for (Iterator<Double> iterator = keysOrdenados.iterator(); iterator.hasNext();) {
			Double chave = iterator.next();
			psIndGeral.println(chave.toString().replace('.', ',') + "\t" + histograma.get(chave));
		}



	}
	
	public double distanciaTchebycheff(double[] z, double[] zEstrela, double[] lambda){
		double distancia = 0;
		
		for (int i = 0; i < z.length; i++) {
			distancia = Math.max(distancia, (1.0/lambda[i]) * Math.abs(zEstrela[i] - z[i]));
		}
		
		return distancia;
	}

	
}