package taintanalysis.rule;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import taintanalysis.config.Config;
import utils.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RuleSelectorManager implements RuleSelector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final List<Rule> rules;

    static {
        try {
            rules = Collections.unmodifiableList(new Gson().fromJson(new InputStreamReader(new ClassPathResource("defaultconfig.json").getInputStream()), Config.class).getRules());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Rule> select(String... cewId) {
        Set<String> cewSet = Arrays.stream(cewId).collect(Collectors.toSet());
        return rules.stream().filter(rule -> cewSet.contains(rule.getRuleCwe())).collect(Collectors.toList());
    }

    @Override
    public List<Rule> all() {
        return rules;
    }

}
