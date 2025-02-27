package io.dataease.provider;

import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.datasource.query.QueryProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import io.dataease.plugins.datasource.provider.Provider;

import java.util.Map;

@Configuration
public class ProviderFactory implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext ctx) {
        this.context =  ctx;
        for(final DatasourceTypes d: DatasourceTypes.values()) {
            final ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
            if(d.isDatasource()){
                DataSourceType dataSourceType = new DataSourceType(d.getType(), d.getName(), false, d.getExtraParams(), d.getCalculationMode(), d.isJdbc());
                if(dataSourceType.getType().equalsIgnoreCase("oracle")){
                    dataSourceType.setCharset(d.getCharset());
                    dataSourceType.setTargetCharset(d.getTargetCharset());
                }
                beanFactory.registerSingleton(d.getType(), dataSourceType);
            }
        }
    }


    public static Provider getProvider(String type) {
        if(type.equalsIgnoreCase(DatasourceTypes.engine_doris.toString()) || type.equalsIgnoreCase(DatasourceTypes.engine_mysql.toString())){
            return context.getBean("jdbc", Provider.class);
        }

        Map<String, DataSourceType> dataSourceTypeMap = SpringContextUtil.getApplicationContext().getBeansOfType((DataSourceType.class));
        if(dataSourceTypeMap.keySet().contains(type)){
            DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
            switch (datasourceType) {
                case es:
                    return context.getBean("esProviders", Provider.class);
                case api:
                    return context.getBean("apiProvider", Provider.class);
                default:
                    return context.getBean("jdbc", Provider.class);
            }
        }

        return SpringContextUtil.getApplicationContext().getBean(type + "DsProvider", Provider.class);

    }

    public static QueryProvider getQueryProvider(String type) {
        switch (type) {
            case "mysql":
            case "mariadb":
            case "ds_doris":
            case "TiDB":
            case "StarRocks":
                return context.getBean("mysqlQueryProvider", QueryProvider.class);
            default:
                return SpringContextUtil.getApplicationContext().getBean(type + "QueryProvider", QueryProvider.class);
        }

    }

    public static DDLProvider getDDLProvider(String type) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case engine_doris:
                return context.getBean("dorisEngineDDL", DDLProvider.class);
            case engine_mysql:
                return context.getBean("mysqlEngineDDL", DDLProvider.class);
            default:
                return context.getBean("dorisEngineDDL", DDLProvider.class);
        }
    }

}
