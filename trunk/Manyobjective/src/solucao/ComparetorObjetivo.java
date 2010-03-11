package solucao;

import java.util.Comparator;

public class ComparetorObjetivo implements Comparator<SolucaoNumerica>{
	
	public int objetivoVez;
	
	public ComparetorObjetivo(int i){
		objetivoVez = i;
	}
	
	public int compare(SolucaoNumerica s1, SolucaoNumerica s2){
		double objetivo1 = s1.objetivos[objetivoVez];
		double objetivo2 = s2.objetivos[objetivoVez];
		if(objetivo1<objetivo2)
			return -1;
		else
			if(objetivo1>objetivo2)
				return 1;
			else
				return 0;
	}

}