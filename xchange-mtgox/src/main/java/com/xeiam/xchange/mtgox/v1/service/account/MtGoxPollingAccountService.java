package com.xeiam.xchange.mtgox.v1.service.account;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;

import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.mtgox.v1.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v1.MtGoxUtils;
import com.xeiam.xchange.mtgox.v1.dto.account.MtGoxAccountInfo;
import com.xeiam.xchange.mtgox.v1.dto.account.MtGoxBitcoinDepositAddress;
import com.xeiam.xchange.mtgox.v1.service.trade.polling.MtGox1;
import com.xeiam.xchange.proxy.HmacPostBodyDigest;
import com.xeiam.xchange.proxy.RestProxyFactory;
import com.xeiam.xchange.service.BasePollingExchangeService;
import com.xeiam.xchange.service.account.polling.PollingAccountService;
import com.xeiam.xchange.utils.Assert;
import com.xeiam.xchange.utils.CryptoUtils;

/**
 * <p>
 * XChange service to provide the following to {@link com.xeiam.xchange.Exchange}:
 * </p>
 * <ul>
 * <li>MtGox specific methods to handle account-related operations</li>
 * </ul>
 */
public class MtGoxPollingAccountService extends BasePollingExchangeService implements PollingAccountService {

  /**
   * Configured from the super class reading of the exchange specification
   */
  private final MtGox1 mtGox1;
  private HmacPostBodyDigest signatureCreator;

  public MtGoxPollingAccountService(ExchangeSpecification exchangeSpecification) {

    super(exchangeSpecification);

    Assert.notNull(exchangeSpecification.getUri(), "Exchange specification URI cannot be null");
    Assert.notNull(exchangeSpecification.getVersion(), "Exchange specification version cannot be null");
    this.mtGox1 = RestProxyFactory.createProxy(MtGox1.class, exchangeSpecification.getUri(), httpTemplate, mapper);
    signatureCreator = new HmacPostBodyDigest(exchangeSpecification.getSecretKey());
  }

  @Override
  public AccountInfo getAccountInfo() {

    MtGoxAccountInfo mtGoxAccountInfo = mtGox1.getAccountInfo(exchangeSpecification.getApiKey(), signatureCreator, getNonce());
    return MtGoxAdapters.adaptAccountInfo(mtGoxAccountInfo);
  }

  @Override
  public String withdrawFunds(BigDecimal amount, String address) {

    Object result = mtGox1.withdrawBtc(
        exchangeSpecification.getApiKey(),
        signatureCreator,
        getNonce(),
        address,
        amount.multiply(new BigDecimal(MtGoxUtils.BTC_VOLUME_AND_AMOUNT_INT_2_DECIMAL_FACTOR)).intValue(), // TODO is the factor OK?
        1, false, false);
    // todo! result as object
    System.out.println(result);
    return result.toString();
  }

  @Override
  public String requestBitcoinDepositAddress(String description, String notificationUrl) {

    MtGoxBitcoinDepositAddress mtGoxBitcoinDepositAddress = mtGox1.requestDepositAddress(
        exchangeSpecification.getApiKey(),
        signatureCreator,
        getNonce(),
        description,
        notificationUrl
    );

    return mtGoxBitcoinDepositAddress.getAddres();
  }

  private long getNonce() {

    return System.currentTimeMillis();
  }
}
