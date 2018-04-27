package worker.tester;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class MainLoad {

	public static void main(final String[] args) throws Exception {

		final ClassLoader bundleLoader = MainLoad.class.getClassLoader();

		final String path = "file:/home/work/sources/git/space-worker/target/classes/";

		final File folder = new File(path);

		final URL workerURL = folder.toURI().toURL();

		System.out.println("workerURL=" + workerURL);

		final URL[] pathArrray = new URL[] { workerURL };

		final URLClassLoader workerLoader = new URLClassLoader(pathArrray, bundleLoader);

		final Class<?> workerClass = workerLoader.loadClass("Arkon");

		System.out.println("workerClass =" + workerClass);

		workerLoader.close();

	}

}
