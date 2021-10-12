package de.nandi.nandicloud.core.completers;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public class ProxyGroupNameCompleter implements Completer {
	@Override
	public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
		list.add(new Candidate("Proxy"));
	}

}
