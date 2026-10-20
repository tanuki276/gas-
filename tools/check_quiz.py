"""Validate bundled JSON and exercise the Java generator without Android dependencies.
Run: python3 tools/check_quiz.py (requires JDK 17+).
"""
import json
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]

def literal(value):
    return json.dumps(value, ensure_ascii=False)

def entities(items):
    values = []
    for item in items:
        values.append('entity(%s, Arrays.asList(%s), %s)' % (
            literal(item['name']), ','.join(map(literal, item.get('tags', []))),
            entities(item.get('sub_entities', []))))
    return 'Arrays.asList(%s)' % ','.join(values)

fixtures = []
for path in sorted((ROOT / 'app/src/main/resources').glob('*.json')):
    data = json.loads(path.read_text())
    periods = ','.join('new Period(%s, %s)' % (
        literal(p['upper_level']), entities(p['middle_level_entities'])) for p in data['periods'])
    fixtures.append('check(%s, data(Arrays.asList(%s)));' % (literal(path.stem), periods))

harness = '''
import java.util.*;
import com.example.historymaster.models.*;
import com.example.historymaster.data.QuizGenerator;
public class QuizCheck {
    static Entity entity(String name, List<String> tags, List<Entity> children) {
        Entity e = new Entity(); e.setName(name); e.setTags(tags); e.setSubEntities(children); return e;
    }
    static HistoryData data(List<Period> periods) {
        HistoryData d = new HistoryData(); d.setPeriods(periods); return d;
    }
    static void require(boolean value) { if (!value) throw new AssertionError(); }
    static void check(String region, HistoryData d) {
        for (int run = 0; run < 100; run++) {
            List<QuizQuestion> pool = QuizGenerator.generateQuiz(d, 100000);
            Set<String> prompts = new HashSet<>();
            for (QuizQuestion q : pool) {
                require(prompts.add(q.getQuestion()));
                require(q.getOptions().size() >= 2 && q.getOptions().size() <= 4);
                require(new HashSet<>(q.getOptions()).size() == q.getOptions().size());
                require(q.getCorrectIndex() >= 0 && q.getCorrectIndex() < q.getOptions().size());
                require(q.checkAnswer(q.getCorrectIndex()));
                require(q.getExplanation() != null);
            }
            require(QuizGenerator.generateQuiz(d, 30).size() == Math.min(30, pool.size()));
            require(QuizGenerator.generateQuiz(d, 50).size() == Math.min(50, pool.size()));
            require(QuizGenerator.generateQuiz(d, 0).isEmpty());
            require(QuizGenerator.generateQuiz(d, -1).isEmpty());
            if (run == 0) System.out.println(region + ": " + pool.size() + " unique prompts");
        }
    }
    public static void main(String[] args) {
        require(QuizGenerator.generateQuiz(null, 50).isEmpty());
        check("empty", new HistoryData());
        check("single", data(Arrays.asList(new Period("era", Arrays.asList(entity("A", Arrays.asList("x"), null))))));
        HistoryData shared = data(Arrays.asList(new Period("era", Arrays.asList(
            entity("A", Arrays.asList("shared"), null), entity("B", Arrays.asList("shared"), null),
            entity("C", Arrays.asList("other"), null)))));
        for (QuizQuestion q : QuizGenerator.generateQuiz(shared, 100)) {
            if (q.getQuestion().contains("「shared」")) {
                require(q.getOptions().size() == 2);
                require(q.getOptions().contains("C"));
            }
        }
        FIXTURES
    }
}
'''.replace('FIXTURES', '\n'.join(fixtures))
with tempfile.TemporaryDirectory() as tmp:
    source = Path(tmp) / 'QuizCheck.java'
    source.write_text(harness)
    base = ROOT / 'app/src/main/java/com/example/historymaster'
    files = list((base / 'models').glob('*.java')) + [base / 'data/QuizGenerator.java', source]
    subprocess.run(['java', '-m', 'jdk.compiler/com.sun.tools.javac.Main', '-encoding', 'UTF-8',
                    '-d', tmp, *map(str, files)], check=True)
    subprocess.run(['java', '-cp', tmp, 'QuizCheck'], check=True, timeout=30)
